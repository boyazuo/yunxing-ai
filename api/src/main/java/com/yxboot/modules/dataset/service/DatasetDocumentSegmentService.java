package com.yxboot.modules.dataset.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.llm.client.embedding.EmbeddingClient;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.llm.storage.VectorStore;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.service.ProviderService;
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import com.yxboot.modules.dataset.mapper.DatasetDocumentSegmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档分段服务实现类
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetDocumentSegmentService extends ServiceImpl<DatasetDocumentSegmentMapper, DatasetDocumentSegment> {

    private final VectorStore vectorStore;
    private final EmbeddingClient embeddingClient;
    private final ProviderService providerService;
    private final DatasetService datasetService;

    /**
     * 批量创建文档分段
     * 
     * @param documentId 文档ID
     * @param segments 分段内容列表
     * @param segmentTitles 分段标题列表（可选）
     * @return 创建的分段列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<DatasetDocumentSegment> batchCreateSegments(DatasetDocument document, List<DocumentSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return new ArrayList<>();
        }

        Long tenantId = document.getTenantId();
        Long datasetId = document.getDatasetId();

        // 批量插入新分段
        List<DatasetDocumentSegment> segmentList = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            DocumentSegment ds = segments.get(i);
            String content = ds.getContent();
            if (content == null || content.trim().isEmpty()) {
                continue;
            }

            DatasetDocumentSegment segment = new DatasetDocumentSegment();
            segment.setTenantId(tenantId);
            segment.setDatasetId(datasetId);
            segment.setDocumentId(document.getDocumentId());
            segment.setVectorId(ds.getId());
            segment.setPosition(i);
            segment.setTitle(ds.getTitle());
            segment.setContent(content);
            segment.setContentLength(content.length());

            segmentList.add(segment);
        }

        // 批量保存
        saveBatch(segmentList);

        return segmentList;
    }

    /**
     * 批量更新分段的向量ID
     * 
     * @param segments 分段列表
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateVectorIds(List<DatasetDocumentSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return true;
        }

        return updateBatchById(segments);
    }

    /**
     * 获取文档的所有分段
     * 
     * @param documentId 文档ID
     * @return 分段列表
     */
    public List<DatasetDocumentSegmentDTO> getSegmentsByDocumentId(Long documentId) {
        return baseMapper.getSegmentsByDocumentId(documentId);
    }

    /**
     * 分页获取文档的分段
     * 
     * @param page 页码
     * @param size 每页大小
     * @param documentId 文档ID
     * @return 分页结果
     */
    public IPage<DatasetDocumentSegmentDTO> pageSegmentsByDocumentId(int page, int size, Long documentId) {
        Page<DatasetDocumentSegmentDTO> pageParam = new Page<>(page, size);
        return baseMapper.pageSegmentsByDocumentId(pageParam, documentId);
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
        Page<DatasetDocumentSegmentDTO> pageParam = new Page<>(current, size);
        return baseMapper.pageSegmentsWithSearch(pageParam, documentId, keyword);
    }

    /**
     * 根据知识库ID获取所有分段
     * 
     * @param datasetId 知识库ID
     * @return 分段列表
     */
    public List<DatasetDocumentSegmentDTO> getSegmentsByDatasetId(Long datasetId) {
        return baseMapper.getSegmentsByDatasetId(datasetId);
    }

    /**
     * 更新文档分段内容并同步向量
     * 
     * @param segmentId 分段ID
     * @param content 新内容
     * @param title 新标题（可选）
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSegmentContent(Long segmentId, String content, String title) {
        DatasetDocumentSegment segment = getById(segmentId);
        if (segment == null) {
            log.warn("分段不存在, segmentId: {}", segmentId);
            return false;
        }

        try {
            // 1. 更新数据库中的分段内容
            boolean contentChanged = false;
            if (content != null && !content.equals(segment.getContent())) {
                segment.setContent(content);
                segment.setContentLength(content.length());
                contentChanged = true;
            }

            if (title != null && !title.equals(segment.getTitle())) {
                segment.setTitle(title);
                contentChanged = true;
            }

            if (!contentChanged) {
                log.info("分段内容未发生变化, segmentId: {}", segmentId);
                return true;
            }

            boolean dbUpdateSuccess = updateById(segment);
            if (!dbUpdateSuccess) {
                log.error("数据库更新分段失败, segmentId: {}", segmentId);
                return false;
            }

            // 2. 重新生成向量并更新向量库
            updateSegmentVector(segment);

            log.info("分段内容和向量更新成功, segmentId: {}", segmentId);
            return true;
        } catch (Exception e) {
            log.error("更新分段内容失败, segmentId: {}", segmentId, e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }

    /**
     * 删除分段并同步删除向量
     * 
     * @param segmentId 分段ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSegmentWithVector(Long segmentId) {
        DatasetDocumentSegment segment = getById(segmentId);
        if (segment == null) {
            log.warn("分段不存在, segmentId: {}", segmentId);
            return false;
        }

        try {
            // 1. 先删除向量库中的数据
            deleteSegmentVector(segment);

            // 2. 再删除数据库中的分段记录
            boolean dbDeleteSuccess = removeById(segmentId);
            if (!dbDeleteSuccess) {
                log.error("数据库删除分段失败, segmentId: {}", segmentId);
                return false;
            }

            log.info("分段和向量删除成功, segmentId: {}", segmentId);
            return true;
        } catch (Exception e) {
            log.error("删除分段失败, segmentId: {}", segmentId, e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }

    /**
     * 批量删除分段并同步删除向量
     * 
     * @param segmentIds 分段ID列表
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteSegmentsWithVectors(List<Long> segmentIds) {
        if (segmentIds == null || segmentIds.isEmpty()) {
            return true;
        }

        try {
            // 1. 获取所有分段信息
            List<DatasetDocumentSegment> segments = listByIds(segmentIds);
            if (segments.isEmpty()) {
                log.warn("未找到任何分段, segmentIds: {}", segmentIds);
                return false;
            }

            // 2. 按知识库分组，批量删除向量
            Map<Long, List<DatasetDocumentSegment>> segmentsByDataset = new HashMap<>();
            for (DatasetDocumentSegment segment : segments) {
                segmentsByDataset.computeIfAbsent(segment.getDatasetId(), k -> new ArrayList<>()).add(segment);
            }

            for (Map.Entry<Long, List<DatasetDocumentSegment>> entry : segmentsByDataset.entrySet()) {
                Long datasetId = entry.getKey();
                List<DatasetDocumentSegment> datasetSegments = entry.getValue();
                batchDeleteSegmentVectors(datasetId, datasetSegments);
            }

            // 3. 删除数据库中的分段记录
            boolean dbDeleteSuccess = removeByIds(segmentIds);
            if (!dbDeleteSuccess) {
                log.error("批量删除分段失败, segmentIds: {}", segmentIds);
                return false;
            }

            log.info("批量删除分段和向量成功, count: {}", segments.size());
            return true;
        } catch (Exception e) {
            log.error("批量删除分段失败, segmentIds: {}", segmentIds, e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }

    /**
     * 重新生成并更新分段的向量
     * 
     * @param segment 分段对象
     */
    private void updateSegmentVector(DatasetDocumentSegment segment) {
        try {
            // 获取知识库信息
            Dataset dataset = datasetService.getById(segment.getDatasetId());
            if (dataset == null) {
                log.error("知识库不存在, datasetId: {}", segment.getDatasetId());
                return;
            }

            // 获取提供商信息
            Provider provider = providerService.getProviderByModelId(dataset.getEmbeddingModelId());
            if (provider == null) {
                log.error("提供商不存在, embeddingModelId: {}", dataset.getEmbeddingModelId());
                return;
            }

            // 使用 Dataset ID 作为集合名称
            String collectionName = "dataset_" + segment.getDatasetId();

            // 确保集合存在
            int dimension = embeddingClient.getEmbeddingDimension(provider);
            if (!vectorStore.ensureCollection(collectionName, dimension)) {
                log.error("确保集合存在失败, collectionName: {}", collectionName);
                return;
            }

            // 生成新向量
            String textContent = segment.getContent();
            float[] vector = embeddingClient.embed(provider, textContent);

            // 准备元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("document_id", segment.getDocumentId());
            metadata.put("dataset_id", segment.getDatasetId());
            metadata.put("tenant_id", segment.getTenantId());
            metadata.put("title", segment.getTitle());

            // 更新向量（使用向量ID）
            String vectorId = segment.getVectorId();
            boolean updateSuccess = vectorStore.addVector(collectionName, vectorId, vector, metadata, textContent);

            if (updateSuccess) {
                log.info("分段向量更新成功, segmentId: {}, vectorId: {}", segment.getSegmentId(), vectorId);
            } else {
                log.error("分段向量更新失败, segmentId: {}, vectorId: {}", segment.getSegmentId(), vectorId);
            }
        } catch (Exception e) {
            log.error("更新分段向量失败, segmentId: {}", segment.getSegmentId(), e);
            // 不抛出异常，避免影响数据库操作
        }
    }

    /**
     * 删除分段的向量
     * 
     * @param segment 分段对象
     */
    private void deleteSegmentVector(DatasetDocumentSegment segment) {
        try {
            // 使用 Dataset ID 作为集合名称
            String collectionName = "dataset_" + segment.getDatasetId();

            // 检查集合是否存在
            if (!vectorStore.collectionExists(collectionName)) {
                log.warn("向量集合不存在, collectionName: {}", collectionName);
                return;
            }

            // 删除向量（使用向量ID）
            String vectorId = segment.getVectorId();
            boolean deleteSuccess = vectorStore.deleteVector(collectionName, vectorId);

            if (deleteSuccess) {
                log.info("分段向量删除成功, segmentId: {}, vectorId: {}", segment.getSegmentId(), vectorId);
            } else {
                log.warn("分段向量删除失败, segmentId: {}, vectorId: {}", segment.getSegmentId(), vectorId);
            }
        } catch (Exception e) {
            log.error("删除分段向量失败, segmentId: {}", segment.getSegmentId(), e);
            // 不抛出异常，避免影响数据库操作
        }
    }

    /**
     * 批量删除分段的向量
     * 
     * @param datasetId 知识库ID
     * @param segments 分段列表
     */
    private void batchDeleteSegmentVectors(Long datasetId, List<DatasetDocumentSegment> segments) {
        try {
            // 使用 Dataset ID 作为集合名称
            String collectionName = "dataset_" + datasetId;

            // 检查集合是否存在
            if (!vectorStore.collectionExists(collectionName)) {
                log.warn("向量集合不存在, collectionName: {}", collectionName);
                return;
            }

            // 收集所有向量ID
            List<String> vectorIds = new ArrayList<>();
            for (DatasetDocumentSegment segment : segments) {
                if (segment.getVectorId() != null) {
                    vectorIds.add(segment.getVectorId());
                }
            }

            if (vectorIds.isEmpty()) {
                log.warn("没有有效的向量ID, datasetId: {}", datasetId);
                return;
            }

            // 批量删除向量
            int deletedCount = vectorStore.deleteVectors(collectionName, vectorIds);
            log.info("批量删除向量成功, datasetId: {}, 删除数量: {}, 预期数量: {}", datasetId, deletedCount, vectorIds.size());
        } catch (Exception e) {
            log.error("批量删除分段向量失败, datasetId: {}", datasetId, e);
            // 不抛出异常，避免影响数据库操作
        }
    }

    /**
     * 删除文档的所有分段
     * 
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSegmentsByDocumentId(Long documentId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DatasetDocumentSegment> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(DatasetDocumentSegment::getDocumentId, documentId);
        boolean success = remove(queryWrapper);
        return success;
    }
}
