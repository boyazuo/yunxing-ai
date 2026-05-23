package com.yxboot.llm.client.vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import com.yxboot.llm.client.embedding.EmbeddingClient;
import com.yxboot.llm.config.VectorProperties;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.embedding.model.EmbeddingModelFactory;
import com.yxboot.llm.vector.VectorStore;
import com.yxboot.llm.vector.query.QueryResult;
import com.yxboot.llm.vector.query.VectorQuery;
import com.yxboot.llm.vector.qdrant.QdrantConfig;
import com.yxboot.llm.vector.qdrant.QdrantVectorStore;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 向量存储客户端
 * 
 * <p>
 * 专门处理向量相关的操作，避免业务Service层的复杂依赖。 支持动态创建和管理多种向量存储后端。
 * </p>
 * 
 * <p>
 * 重构说明：
 * </p>
 * <ul>
 * <li>不再依赖注入固定的 VectorStore</li>
 * <li>根据配置动态创建 VectorStore 实例</li>
 * <li>支持多种向量存储后端（目前支持 QDrant）</li>
 * <li>提供 VectorStore 实例缓存机制</li>
 * <li>与 EmbeddingClient 协同工作</li>
 * </ul>
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStoreClient {

    private final EmbeddingClient embeddingClient;
    private final EmbeddingModelFactory embeddingModelFactory;
    private final VectorProperties vectorProperties;

    // VectorStore 实例缓存，避免重复创建
    private final Map<String, VectorStore> vectorStoreCache = new ConcurrentHashMap<>();

    /**
     * 获取默认的 QDrant 配置
     * 
     * @return QdrantConfig 实例
     */
    private QdrantConfig getDefaultQdrantConfig() {
        VectorProperties.QdrantProperties qdrantProps = vectorProperties.getQdrant();
        return QdrantConfig.builder()
                .host(qdrantProps.getHost())
                .port(qdrantProps.getPort())
                .apiKey(qdrantProps.getApiKey())
                .defaultCollectionName(qdrantProps.getDefaultCollectionName())
                .vectorName(qdrantProps.getVectorName())
                .build();
    }

    /**
     * 获取或创建 VectorStore 实例
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @return VectorStore 实例
     */
    private VectorStore getVectorStore(Provider provider, Model model) {
        // 生成缓存键，基于提供商、模型和租户ID
        String cacheKey = generateVectorStoreCacheKey(provider, model);

        return vectorStoreCache.computeIfAbsent(cacheKey, key -> {
            log.debug("为提供商 {} 模型 {} (租户: {}) 创建新的VectorStore实例",
                    provider.getProviderName(), model.getModelName(), provider.getTenantId());

            // 创建嵌入模型
            EmbeddingModel embeddingModel = embeddingModelFactory.createEmbeddingModel(provider, model);

            // 目前只支持 QDrant，未来可以根据配置选择不同的向量存储
            return createQdrantVectorStore(embeddingModel, provider);
        });
    }

    /**
     * 创建 QDrant VectorStore 实例
     * 
     * @param embeddingModel 嵌入模型
     * @param provider 提供商信息（用于租户隔离）
     * @return QdrantVectorStore 实例
     */
    private VectorStore createQdrantVectorStore(EmbeddingModel embeddingModel, Provider provider) {
        // 根据租户ID生成默认集合名称，实现租户隔离
        String defaultCollection = "tenant_" + provider.getTenantId() + "_documents";

        return QdrantVectorStore.builder()
                .config(getDefaultQdrantConfig().toBuilder()
                        .defaultCollectionName(defaultCollection)
                        .build())
                .embeddingModel(embeddingModel)
                .autoCreateCollection(true)
                .build();
    }

    /**
     * 生成 VectorStore 缓存键
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @return 缓存键
     */
    private String generateVectorStoreCacheKey(Provider provider, Model model) {
        return String.format("%s:%s:%s:%d",
                provider.getProviderName(),
                model.getModelName(),
                provider.getApiKey().hashCode(),
                provider.getTenantId());
    }

    /**
     * 获取知识库对应的向量集合名称
     *
     * @param datasetId 知识库ID
     * @param tenantId 租户ID
     * @return 集合名称
     */
    public String getCollectionName(Long datasetId, Long tenantId) {
        return generateCollectionName(datasetId, tenantId);
    }

    /**
     * 检查知识库向量集合是否存在
     *
     * @param datasetId 知识库ID
     * @param tenantId 租户ID
     * @return 是否存在
     */
    public boolean collectionExists(Long datasetId, Long tenantId) {
        try {
            VectorStore vectorStore = createDefaultVectorStore();
            return vectorStore.collectionExists(getCollectionName(datasetId, tenantId));
        } catch (Exception e) {
            log.error("检查向量集合是否存在失败, datasetId: {}, 租户: {}", datasetId, tenantId, e);
            return false;
        }
    }

    /**
     * 删除知识库向量集合
     *
     * @param datasetId 知识库ID
     * @param tenantId 租户ID
     * @return 是否成功
     */
    public boolean deleteCollection(Long datasetId, Long tenantId) {
        try {
            VectorStore vectorStore = createDefaultVectorStore();
            String collectionName = getCollectionName(datasetId, tenantId);
            if (!vectorStore.collectionExists(collectionName)) {
                log.info("知识库向量集合不存在, datasetId: {}, collectionName: {}", datasetId, collectionName);
                return true;
            }
            boolean deleted = vectorStore.deleteCollection(collectionName);
            if (deleted) {
                log.info("删除知识库向量集合成功, datasetId: {}, collectionName: {}", datasetId, collectionName);
            } else {
                log.warn("删除知识库向量集合失败, datasetId: {}, collectionName: {}", datasetId, collectionName);
            }
            return deleted;
        } catch (Exception e) {
            log.error("删除知识库向量集合失败, datasetId: {}, 租户: {}", datasetId, tenantId, e);
            return false;
        }
    }

    /**
     * 在指定知识库中执行向量相似度搜索
     *
     * @param datasetId 知识库ID
     * @param tenantId 租户ID
     * @param provider 提供商信息
     * @param model 模型信息
     * @param query 查询参数
     * @return 检索结果列表
     */
    public List<QueryResult> similaritySearch(Long datasetId, Long tenantId, Provider provider, Model model,
            VectorQuery query) {
        VectorStore vectorStore = getVectorStore(provider, model);
        query.setCollectionName(getCollectionName(datasetId, tenantId));
        return vectorStore.similaritySearch(query);
    }

    /**
     * 确保向量集合存在
     * 
     * @param datasetId 知识库ID
     * @param provider 提供商信息
     * @param model 模型信息
     * @return 是否成功
     */
    public boolean ensureVectorCollection(Long datasetId, Provider provider, Model model) {
        try {
            VectorStore vectorStore = getVectorStore(provider, model);
            String collectionName = generateCollectionName(datasetId, provider.getTenantId());
            int dimension = embeddingClient.getEmbeddingDimension(provider, model);

            return vectorStore.ensureCollection(collectionName, dimension);
        } catch (Exception e) {
            log.error("确保向量集合存在失败, datasetId: {}, 租户: {}", datasetId, provider.getTenantId(), e);
            return false;
        }
    }

    /**
     * 为分段生成并存储向量
     * 
     * @param segment 分段对象
     * @param provider 提供商信息
     * @param model 模型信息
     * @return 是否成功
     */
    public boolean createSegmentVector(DatasetDocumentSegment segment, Provider provider, Model model) {
        try {
            if (provider == null) {
                log.error("提供商信息为空, segmentId: {}", segment.getSegmentId());
                return false;
            }
            if (model == null) {
                log.error("模型信息为空, segmentId: {}", segment.getSegmentId());
                return false;
            }

            VectorStore vectorStore = getVectorStore(provider, model);
            String collectionName = generateCollectionName(segment.getDatasetId(), segment.getTenantId());

            // 确保集合存在
            if (!ensureVectorCollection(segment.getDatasetId(), provider, model)) {
                log.error("确保集合存在失败, datasetId: {}, 租户: {}", segment.getDatasetId(), segment.getTenantId());
                return false;
            }

            // 生成向量
            float[] vector = embeddingClient.embed(provider, model, segment.getContent());

            // 准备元数据
            Map<String, Object> metadata = createSegmentMetadata(segment);

            // 存储向量
            boolean success = vectorStore.addVector(collectionName, segment.getVectorId(), vector, metadata,
                    segment.getContent());

            if (success) {
                log.debug("分段向量创建成功, segmentId: {}, vectorId: {}, 租户: {}",
                        segment.getSegmentId(), segment.getVectorId(), segment.getTenantId());
            } else {
                log.error("分段向量创建失败, segmentId: {}, vectorId: {}, 租户: {}",
                        segment.getSegmentId(), segment.getVectorId(), segment.getTenantId());
            }

            return success;
        } catch (Exception e) {
            log.error("创建分段向量失败, segmentId: {}, 租户: {}", segment.getSegmentId(), segment.getTenantId(), e);
            return false;
        }
    }

    /**
     * 批量为分段生成并存储向量
     * 
     * @param segments 分段列表
     * @param datasetId 知识库ID
     * @param provider 提供商信息
     * @param model 模型信息
     * @return 成功处理的数量
     */
    public int batchCreateSegmentVectors(List<DatasetDocumentSegment> segments, Long datasetId, Provider provider,
            Model model) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }

        try {
            VectorStore vectorStore = getVectorStore(provider, model);
            Long tenantId = segments.get(0).getTenantId(); // 假设同批次的分段属于同一租户
            String collectionName = generateCollectionName(datasetId, tenantId);

            // 确保集合存在
            if (!ensureVectorCollection(datasetId, provider, model)) {
                log.error("确保集合存在失败, datasetId: {}, 租户: {}", datasetId, tenantId);
                return 0;
            }

            // 准备批量数据
            List<String> ids = segments.stream().map(DatasetDocumentSegment::getVectorId).toList();
            List<String> texts = segments.stream().map(DatasetDocumentSegment::getContent).toList();
            List<Map<String, Object>> metadataList = segments.stream()
                    .map(this::createSegmentMetadata)
                    .toList();

            // 批量生成向量
            List<float[]> vectors = embeddingClient.embedAll(provider, model, texts);

            // 批量存储向量
            int successCount = vectorStore.addVectors(collectionName, ids, vectors, metadataList, texts);

            log.info("批量创建分段向量完成, datasetId: {}, 租户: {}, 成功: {}, 总数: {}",
                    datasetId, tenantId, successCount, segments.size());
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
     * @param model 模型信息
     * @return 是否成功
     */
    public boolean updateSegmentVector(DatasetDocumentSegment segment, Provider provider, Model model) {
        // 更新操作其实就是重新创建
        return createSegmentVector(segment, provider, model);
    }

    /**
     * 删除分段向量
     * 
     * @param segment 分段对象
     * @return 是否成功
     */
    public boolean deleteSegmentVector(DatasetDocumentSegment segment) {
        try {
            // 使用默认配置创建临时 VectorStore 实例进行删除操作
            VectorStore vectorStore = createDefaultVectorStore();
            String collectionName = generateCollectionName(segment.getDatasetId(), segment.getTenantId());

            // 检查集合是否存在
            if (!vectorStore.collectionExists(collectionName)) {
                log.warn("向量集合不存在, collectionName: {}, 租户: {}", collectionName, segment.getTenantId());
                return true; // 集合不存在认为删除成功
            }

            boolean success = vectorStore.deleteVector(collectionName, segment.getVectorId());

            if (success) {
                log.debug("分段向量删除成功, segmentId: {}, vectorId: {}, 租户: {}",
                        segment.getSegmentId(), segment.getVectorId(), segment.getTenantId());
            } else {
                log.warn("分段向量删除失败, segmentId: {}, vectorId: {}, 租户: {}",
                        segment.getSegmentId(), segment.getVectorId(), segment.getTenantId());
            }

            return success;
        } catch (Exception e) {
            log.error("删除分段向量失败, segmentId: {}, 租户: {}", segment.getSegmentId(), segment.getTenantId(), e);
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
            VectorStore vectorStore = createDefaultVectorStore();
            Long tenantId = segments.get(0).getTenantId(); // 假设同批次的分段属于同一租户
            String collectionName = generateCollectionName(datasetId, tenantId);

            // 检查集合是否存在
            if (!vectorStore.collectionExists(collectionName)) {
                log.warn("向量集合不存在, collectionName: {}, 租户: {}", collectionName, tenantId);
                return segments.size(); // 集合不存在认为删除成功
            }

            // 收集向量ID
            List<String> vectorIds = segments.stream()
                    .map(DatasetDocumentSegment::getVectorId)
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .toList();

            if (vectorIds.isEmpty()) {
                log.warn("没有有效的向量ID, datasetId: {}, 租户: {}", datasetId, tenantId);
                return 0;
            }

            // 批量删除
            int deletedCount = vectorStore.deleteVectors(collectionName, vectorIds);
            log.info("批量删除分段向量完成, datasetId: {}, 租户: {}, 删除数量: {}, 预期数量: {}",
                    datasetId, tenantId, deletedCount, vectorIds.size());

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
     * @param tenantId 租户ID
     * @param filter 过滤条件
     * @return 删除的数量
     */
    public int deleteVectorsByFilter(Long datasetId, Long tenantId, Map<String, Object> filter) {
        try {
            VectorStore vectorStore = createDefaultVectorStore();
            String collectionName = generateCollectionName(datasetId, tenantId);

            // 检查集合是否存在
            if (!vectorStore.collectionExists(collectionName)) {
                log.warn("向量集合不存在, collectionName: {}, 租户: {}", collectionName, tenantId);
                return 0;
            }

            // 添加租户隔离过滤条件
            Map<String, Object> enhancedFilter = new HashMap<>(filter);
            enhancedFilter.put("tenant_id", tenantId);

            int deletedCount = vectorStore.deleteVectorsByFilter(collectionName, enhancedFilter);
            log.info("按条件删除向量完成, datasetId: {}, 租户: {}, 删除数量: {}, 过滤条件: {}",
                    datasetId, tenantId, deletedCount, enhancedFilter);

            return deletedCount;
        } catch (Exception e) {
            log.error("按条件删除向量失败, datasetId: {}, 租户: {}, 过滤条件: {}", datasetId, tenantId, filter, e);
            return 0;
        }
    }

    /**
     * 删除文档的所有向量
     * 
     * @param documentId 文档ID
     * @param datasetId 知识库ID
     * @param tenantId 租户ID
     * @return 删除的向量数量
     */
    public int deleteDocumentVectors(Long documentId, Long datasetId, Long tenantId) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("document_id", documentId);

        int deletedCount = deleteVectorsByFilter(datasetId, tenantId, filter);
        log.info("删除文档向量数据, documentId: {}, datasetId: {}, 租户: {}, 删除数量: {}",
                documentId, datasetId, tenantId, deletedCount);

        return deletedCount;
    }

    /**
     * 清除指定提供商和模型的 VectorStore 缓存
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     */
    public void clearVectorStoreCache(Provider provider, Model model) {
        String cacheKey = generateVectorStoreCacheKey(provider, model);
        vectorStoreCache.remove(cacheKey);
        log.debug("已清除VectorStore缓存, 提供商: {}, 模型: {}, 租户: {}",
                provider.getProviderName(), model.getModelName(), provider.getTenantId());
    }

    /**
     * 清除所有 VectorStore 缓存
     */
    public void clearAllVectorStoreCache() {
        int size = vectorStoreCache.size();
        vectorStoreCache.clear();
        log.debug("已清除所有VectorStore缓存，共 {} 个实例", size);
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("vectorStoreCacheSize", vectorStoreCache.size());
        return stats;
    }

    /**
     * 创建默认的 VectorStore 实例（用于删除等不需要特定模型的操作）
     * 
     * @return VectorStore 实例
     */
    private VectorStore createDefaultVectorStore() {
        // 创建一个临时的提供商对象（删除操作不需要实际使用）
        Provider dummyProvider = new Provider();
        dummyProvider.setProviderName("zhipu");
        dummyProvider.setApiKey("dummy");
        dummyProvider.setTenantId(0L);

        // 创建一个临时的模型对象
        Model dummyModel = new Model();
        dummyModel.setModelName("embedding-2");

        // 创建嵌入模型
        EmbeddingModel embeddingModel = embeddingModelFactory.createEmbeddingModel(dummyProvider, dummyModel);

        return QdrantVectorStore.builder()
                .config(getDefaultQdrantConfig())
                .embeddingModel(embeddingModel)
                .autoCreateCollection(false) // 删除操作不需要自动创建集合
                .build();
    }

    /**
     * 生成集合名称（包含租户隔离）
     * 
     * @param datasetId 知识库ID
     * @param tenantId 租户ID
     * @return 集合名称
     */
    private String generateCollectionName(Long datasetId, Long tenantId) {
        return String.format("tenant_%d_dataset_%d", tenantId, datasetId);
    }

    /**
     * 创建分段元数据
     * 
     * @param segment 分段对象
     * @return 元数据Map
     */
    private Map<String, Object> createSegmentMetadata(DatasetDocumentSegment segment) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("document_id", segment.getDocumentId());
        metadata.put("dataset_id", segment.getDatasetId());
        metadata.put("tenant_id", segment.getTenantId());
        metadata.put("title", segment.getTitle());
        metadata.put("segment_id", segment.getSegmentId());
        return metadata;
    }
}
