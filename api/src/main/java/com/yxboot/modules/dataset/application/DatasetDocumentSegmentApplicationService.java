package com.yxboot.modules.dataset.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yxboot.llm.client.storage.VectorClient;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.service.ProviderService;
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import com.yxboot.modules.dataset.service.DatasetDocumentSegmentService;
import com.yxboot.modules.dataset.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档分段应用服务 负责分段相关的业务流程编排，协调多个领域服务
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetDocumentSegmentApplicationService {

    private final DatasetDocumentSegmentService segmentService;
    private final VectorClient vectorClient;
    private final ProviderService providerService;
    private final DatasetService datasetService;

    /**
     * 更新分段内容并同步向量 协调数据库更新和向量更新
     * 
     * @param segmentId 分段ID
     * @param content 新内容
     * @param title 新标题（可选）
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSegmentContent(Long segmentId, String content, String title) {
        log.info("更新分段内容, segmentId: {}", segmentId);

        // 1. 更新数据库中的分段内容
        boolean dbUpdateSuccess = segmentService.updateSegmentContent(segmentId, content, title);
        if (!dbUpdateSuccess) {
            log.error("数据库更新分段失败, segmentId: {}", segmentId);
            return false;
        }

        // 2. 更新向量
        DatasetDocumentSegment segment = segmentService.getById(segmentId);
        if (segment != null) {
            // 获取知识库和提供商信息
            Dataset dataset = datasetService.getById(segment.getDatasetId());
            if (dataset != null) {
                Provider provider = providerService.getProviderByModelId(dataset.getEmbeddingModelId());
                if (provider != null) {
                    boolean vectorUpdateSuccess = vectorClient.updateSegmentVector(segment, provider);
                    if (!vectorUpdateSuccess) {
                        log.warn("向量更新失败, segmentId: {}, 但数据库更新成功", segmentId);
                    }
                } else {
                    log.warn("提供商不存在, embeddingModelId: {}, 跳过向量更新", dataset.getEmbeddingModelId());
                }
            } else {
                log.warn("知识库不存在, datasetId: {}, 跳过向量更新", segment.getDatasetId());
            }
        }

        return true;
    }

    /**
     * 删除分段并同步删除向量 协调向量删除和数据库删除
     * 
     * @param segmentId 分段ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSegmentCompletely(Long segmentId) {
        log.info("删除分段及向量, segmentId: {}", segmentId);

        // 1. 获取分段信息用于删除向量
        DatasetDocumentSegment segment = segmentService.getById(segmentId);
        if (segment == null) {
            log.warn("分段不存在, segmentId: {}", segmentId);
            return false;
        }

        // 2. 删除向量
        boolean vectorDeleteSuccess = vectorClient.deleteSegmentVector(segment);
        if (!vectorDeleteSuccess) {
            log.warn("向量删除失败, segmentId: {}", segmentId);
        }

        // 3. 删除数据库记录
        boolean dbDeleteSuccess = segmentService.deleteSegment(segmentId);
        if (!dbDeleteSuccess) {
            log.error("数据库删除分段失败, segmentId: {}", segmentId);
            return false;
        }

        return true;
    }

    /**
     * 批量删除分段并同步删除向量 协调批量向量删除和数据库删除
     * 
     * @param segmentIds 分段ID列表
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteSegmentsCompletely(List<Long> segmentIds) {
        log.info("批量删除分段及向量, count: {}", segmentIds.size());

        if (segmentIds == null || segmentIds.isEmpty()) {
            return true;
        }

        // 1. 获取所有分段信息
        List<DatasetDocumentSegment> segments = segmentService.listByIds(segmentIds);
        if (segments.isEmpty()) {
            log.warn("未找到任何分段, segmentIds: {}", segmentIds);
            return false;
        }

        // 2. 按知识库分组批量删除向量
        segments.stream().collect(java.util.stream.Collectors.groupingBy(DatasetDocumentSegment::getDatasetId))
                .forEach((datasetId, datasetSegments) -> {
                    int deletedCount = vectorClient.batchDeleteSegmentVectors(datasetSegments, datasetId);
                    log.info("批量删除向量, datasetId: {}, 删除数量: {}", datasetId, deletedCount);
                });

        // 3. 删除数据库记录
        boolean dbDeleteSuccess = segmentService.batchDeleteSegments(segmentIds);
        if (!dbDeleteSuccess) {
            log.error("批量删除分段失败, segmentIds: {}", segmentIds);
            return false;
        }

        return true;
    }

    /**
     * 获取文档的所有分段
     * 
     * @param documentId 文档ID
     * @return 分段列表
     */
    public List<DatasetDocumentSegmentDTO> getSegmentsByDocumentId(Long documentId) {
        return segmentService.getSegmentsByDocumentId(documentId);
    }

    /**
     * 分页获取文档的分段
     * 
     * @param current 页码
     * @param size 每页大小
     * @param documentId 文档ID
     * @return 分页结果
     */
    public IPage<DatasetDocumentSegmentDTO> pageSegmentsByDocumentId(long current, long size, Long documentId) {
        return segmentService.pageSegmentsByDocumentId((int) current, (int) size, documentId);
    }

    /**
     * 分页获取文档的分段（带搜索）
     * 
     * @param current 页码
     * @param size 每页大小
     * @param documentId 文档ID
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    public IPage<DatasetDocumentSegmentDTO> pageSegmentsWithSearch(long current, long size, Long documentId, String keyword) {
        return segmentService.pageSegmentsWithSearch(current, size, documentId, keyword);
    }

    /**
     * 根据知识库ID获取所有分段
     * 
     * @param datasetId 知识库ID
     * @return 分段列表
     */
    public List<DatasetDocumentSegmentDTO> getSegmentsByDatasetId(Long datasetId) {
        return segmentService.getSegmentsByDatasetId(datasetId);
    }

    /**
     * 根据分段ID获取分段详情
     * 
     * @param segmentId 分段ID
     * @return 分段对象
     */
    public DatasetDocumentSegment getSegmentById(Long segmentId) {
        return segmentService.getById(segmentId);
    }
}
