package com.yxboot.llm.client.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.yxboot.llm.client.embedding.EmbeddingClient;
import com.yxboot.llm.storage.VectorStore;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 向量存储客户端 专门处理向量相关的操作，避免业务Service层的复杂依赖
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorClient {

    private final VectorStore vectorStore;
    private final EmbeddingClient embeddingClient;

    /**
     * 确保向量集合存在
     * 
     * @param datasetId 知识库ID
     * @param provider 提供商信息
     * @return 是否成功
     */
    public boolean ensureVectorCollection(Long datasetId, Provider provider) {
        try {
            if (provider == null) {
                log.error("提供商信息为空, datasetId: {}", datasetId);
                return false;
            }

            String collectionName = "dataset_" + datasetId;
            int dimension = embeddingClient.getEmbeddingDimension(provider);

            return vectorStore.ensureCollection(collectionName, dimension);
        } catch (Exception e) {
            log.error("确保向量集合存在失败, datasetId: {}", datasetId, e);
            return false;
        }
    }

    /**
     * 为分段生成并存储向量
     * 
     * @param segment 分段对象
     * @param provider 提供商信息
     * @return 是否成功
     */
    public boolean createSegmentVector(DatasetDocumentSegment segment, Provider provider) {
        try {
            if (provider == null) {
                log.error("提供商信息为空, segmentId: {}", segment.getSegmentId());
                return false;
            }

            String collectionName = "dataset_" + segment.getDatasetId();

            // 确保集合存在
            if (!ensureVectorCollection(segment.getDatasetId(), provider)) {
                log.error("确保集合存在失败, datasetId: {}", segment.getDatasetId());
                return false;
            }

            // 生成向量
            float[] vector = embeddingClient.embed(provider, segment.getContent());

            // 准备元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("document_id", segment.getDocumentId());
            metadata.put("dataset_id", segment.getDatasetId());
            metadata.put("tenant_id", segment.getTenantId());
            metadata.put("title", segment.getTitle());

            // 存储向量
            boolean success = vectorStore.addVector(collectionName, segment.getVectorId(), vector, metadata, segment.getContent());

            if (success) {
                log.debug("分段向量创建成功, segmentId: {}, vectorId: {}", segment.getSegmentId(), segment.getVectorId());
            } else {
                log.error("分段向量创建失败, segmentId: {}, vectorId: {}", segment.getSegmentId(), segment.getVectorId());
            }

            return success;
        } catch (Exception e) {
            log.error("创建分段向量失败, segmentId: {}", segment.getSegmentId(), e);
            return false;
        }
    }

    /**
     * 批量为分段生成并存储向量
     * 
     * @param segments 分段列表
     * @param datasetId 知识库ID
     * @param provider 提供商信息
     * @return 成功处理的数量
     */
    public int batchCreateSegmentVectors(List<DatasetDocumentSegment> segments, Long datasetId, Provider provider) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }

        try {
            if (provider == null) {
                log.error("提供商信息为空, datasetId: {}", datasetId);
                return 0;
            }

            String collectionName = "dataset_" + datasetId;

            // 确保集合存在
            if (!ensureVectorCollection(datasetId, provider)) {
                log.error("确保集合存在失败, datasetId: {}", datasetId);
                return 0;
            }

            // 准备批量向量化的数据
            List<String> texts = segments.stream().map(DatasetDocumentSegment::getContent).toList();

            // 批量生成向量
            List<float[]> vectors = embeddingClient.embedAll(provider, texts);

            // 批量存储
            int successCount = 0;
            for (int i = 0; i < segments.size(); i++) {
                DatasetDocumentSegment segment = segments.get(i);
                float[] vector = vectors.get(i);

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("document_id", segment.getDocumentId());
                metadata.put("dataset_id", segment.getDatasetId());
                metadata.put("tenant_id", segment.getTenantId());
                metadata.put("title", segment.getTitle());

                boolean success = vectorStore.addVector(collectionName, segment.getVectorId(), vector, metadata, segment.getContent());
                if (success) {
                    successCount++;
                }
            }

            log.info("批量创建分段向量完成, datasetId: {}, 成功: {}, 总数: {}", datasetId, successCount, segments.size());
            return successCount;
        } catch (Exception e) {
            log.error("批量创建分段向量失败, datasetId: {}", datasetId, e);
            return 0;
        }
    }

    /**
     * 更新分段向量
     * 
     * @param segment 分段对象
     * @param provider 提供商信息
     * @return 是否成功
     */
    public boolean updateSegmentVector(DatasetDocumentSegment segment, Provider provider) {
        // 更新操作其实就是重新创建
        return createSegmentVector(segment, provider);
    }

    /**
     * 删除分段向量
     * 
     * @param segment 分段对象
     * @return 是否成功
     */
    public boolean deleteSegmentVector(DatasetDocumentSegment segment) {
        try {
            String collectionName = "dataset_" + segment.getDatasetId();

            // 检查集合是否存在
            if (!vectorStore.collectionExists(collectionName)) {
                log.warn("向量集合不存在, collectionName: {}", collectionName);
                return true; // 集合不存在认为删除成功
            }

            boolean success = vectorStore.deleteVector(collectionName, segment.getVectorId());

            if (success) {
                log.debug("分段向量删除成功, segmentId: {}, vectorId: {}", segment.getSegmentId(), segment.getVectorId());
            } else {
                log.warn("分段向量删除失败, segmentId: {}, vectorId: {}", segment.getSegmentId(), segment.getVectorId());
            }

            return success;
        } catch (Exception e) {
            log.error("删除分段向量失败, segmentId: {}", segment.getSegmentId(), e);
            return false;
        }
    }

    /**
     * 批量删除分段向量
     * 
     * @param segments 分段列表
     * @param datasetId 知识库ID
     * @return 删除成功的数量
     */
    public int batchDeleteSegmentVectors(List<DatasetDocumentSegment> segments, Long datasetId) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }

        try {
            String collectionName = "dataset_" + datasetId;

            // 检查集合是否存在
            if (!vectorStore.collectionExists(collectionName)) {
                log.warn("向量集合不存在, collectionName: {}", collectionName);
                return segments.size(); // 集合不存在认为删除成功
            }

            // 收集向量ID
            List<String> vectorIds =
                    segments.stream().map(DatasetDocumentSegment::getVectorId).filter(id -> id != null && !id.trim().isEmpty()).toList();

            if (vectorIds.isEmpty()) {
                log.warn("没有有效的向量ID, datasetId: {}", datasetId);
                return 0;
            }

            // 批量删除
            int deletedCount = vectorStore.deleteVectors(collectionName, vectorIds);
            log.info("批量删除分段向量完成, datasetId: {}, 删除数量: {}, 预期数量: {}", datasetId, deletedCount, vectorIds.size());

            return deletedCount;
        } catch (Exception e) {
            log.error("批量删除分段向量失败, datasetId: {}", datasetId, e);
            return 0;
        }
    }

    /**
     * 按条件删除向量
     * 
     * @param datasetId 知识库ID
     * @param filter 过滤条件
     * @return 删除的数量
     */
    public int deleteVectorsByFilter(Long datasetId, Map<String, Object> filter) {
        try {
            String collectionName = "dataset_" + datasetId;

            // 检查集合是否存在
            if (!vectorStore.collectionExists(collectionName)) {
                log.warn("向量集合不存在, collectionName: {}", collectionName);
                return 0;
            }

            int deletedCount = vectorStore.deleteVectorsByFilter(collectionName, filter);
            log.info("按条件删除向量完成, datasetId: {}, 删除数量: {}, 过滤条件: {}", datasetId, deletedCount, filter);

            return deletedCount;
        } catch (Exception e) {
            log.error("按条件删除向量失败, datasetId: {}, 过滤条件: {}", datasetId, filter, e);
            return 0;
        }
    }

    /**
     * 删除文档的所有向量
     * 
     * @param documentId 文档ID
     * @param datasetId 知识库ID
     * @return 删除的向量数量
     */
    public int deleteDocumentVectors(Long documentId, Long datasetId) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("document_id", documentId);

        int deletedCount = deleteVectorsByFilter(datasetId, filter);
        log.info("删除文档向量数据, documentId: {}, datasetId: {}, 删除数量: {}", documentId, datasetId, deletedCount);

        return deletedCount;
    }
}
