package com.yxboot.modules.dataset.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mybatisflex.core.paginate.Page;
import com.yxboot.ai.service.AiVectorStoreService;
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
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
    private final AiVectorStoreService vectorStoreService;
    private final DatasetService datasetService;

    @Transactional(rollbackFor = Exception.class)
    public boolean updateSegmentContent(Long segmentId, String content, String title) {
        log.info("更新分段内容, segmentId: {}", segmentId);

        boolean dbUpdateSuccess = segmentService.updateSegmentContent(segmentId, content, title);
        if (!dbUpdateSuccess) {
            log.error("数据库更新分段失败, segmentId: {}", segmentId);
            return false;
        }

        DatasetDocumentSegment segment = segmentService.getById(segmentId);
        if (segment != null) {
            boolean vectorUpdateSuccess = vectorStoreService.updateSegmentVector(segment);
            if (!vectorUpdateSuccess) {
                log.warn("向量更新失败, segmentId: {}, 但数据库更新成功", segmentId);
            } else {
                datasetService.recordEmbeddingModel(segment.getDatasetId());
            }
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSegmentCompletely(Long segmentId) {
        log.info("删除分段及向量, segmentId: {}", segmentId);

        DatasetDocumentSegment segment = segmentService.getById(segmentId);
        if (segment == null) {
            log.warn("分段不存在, segmentId: {}", segmentId);
            return false;
        }

        boolean vectorDeleteSuccess = vectorStoreService.deleteSegmentVector(segment);
        if (!vectorDeleteSuccess) {
            log.warn("向量删除失败, segmentId: {}", segmentId);
        }

        boolean dbDeleteSuccess = segmentService.deleteSegment(segmentId);
        if (!dbDeleteSuccess) {
            log.error("数据库删除分段失败, segmentId: {}", segmentId);
            return false;
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteSegmentsCompletely(List<Long> segmentIds) {
        log.info("批量删除分段及向量, count: {}", segmentIds.size());

        if (segmentIds == null || segmentIds.isEmpty()) {
            return true;
        }

        List<DatasetDocumentSegment> segments = segmentService.listByIds(segmentIds);
        if (segments.isEmpty()) {
            log.warn("未找到任何分段, segmentIds: {}", segmentIds);
            return false;
        }

        segments.stream().collect(java.util.stream.Collectors.groupingBy(DatasetDocumentSegment::getDatasetId))
                .forEach((datasetId, datasetSegments) -> {
                    int deletedCount = vectorStoreService.batchDeleteSegmentVectors(datasetSegments, datasetId);
                    log.info("批量删除向量, datasetId: {}, 删除数量: {}", datasetId, deletedCount);
                });

        boolean dbDeleteSuccess = segmentService.batchDeleteSegments(segmentIds);
        if (!dbDeleteSuccess) {
            log.error("批量删除分段失败, segmentIds: {}", segmentIds);
            return false;
        }

        return true;
    }

    public List<DatasetDocumentSegmentDTO> getSegmentsByDocumentId(Long documentId) {
        return segmentService.getSegmentsByDocumentId(documentId);
    }

    public Page<DatasetDocumentSegmentDTO> pageSegmentsByDocumentId(long current, long size, Long documentId) {
        return segmentService.pageSegmentsByDocumentId((int) current, (int) size, documentId);
    }

    public Page<DatasetDocumentSegmentDTO> pageSegmentsWithSearch(long current, long size, Long documentId,
            String keyword) {
        return segmentService.pageSegmentsWithSearch(current, size, documentId, keyword);
    }

    public List<DatasetDocumentSegmentDTO> getSegmentsByDatasetId(Long datasetId) {
        return segmentService.getSegmentsByDatasetId(datasetId);
    }

    public DatasetDocumentSegment getSegmentById(Long segmentId) {
        return segmentService.getById(segmentId);
    }
}
