package com.yxboot.llm.client.vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import com.yxboot.llm.client.embedding.EmbeddingClient;
import com.yxboot.llm.vector.VectorStore;
import com.yxboot.llm.vector.query.QueryResult;
import com.yxboot.llm.vector.query.VectorQuery;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.service.ModelService;
import com.yxboot.modules.ai.service.ProviderService;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 向量检索客户端 作为连接通用向量存储层和业务层的桥梁 提供简化的向量检索API，隐藏底层复杂性
 * 
 * 架构层次： 1. 向量存储底层 (VectorStore) 2. 嵌入处理层 (EmbeddingClient) 3. 连接层 (VectorRetrieverClient) ← 当前类 4.
 * 业务逻辑层 (上层应用的业务层)
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorRetrieverClient {

    private final VectorStore vectorStore;
    private final EmbeddingClient embeddingClient;
    private final DatasetService datasetService;
    private final ProviderService providerService;
    private final ModelService modelService;
    private final VectorRetrieverConfig config;

    // 缓存已获取的Provider信息，避免重复查询
    private final Map<Long, Provider> providerCache = new ConcurrentHashMap<>();

    /**
     * 简单文本检索
     * 
     * @param datasetId 知识库ID
     * @param query 查询文本
     * @return 检索结果列表
     */
    public List<QueryResult> retrieve(Long datasetId, String query) {
        return retrieve(datasetId, query, config.getDefaultLimit(), config.getDefaultMinScore());
    }

    /**
     * 文本检索（带参数）
     * 
     * @param datasetId 知识库ID
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @param minScore 最小相似度阈值
     * @return 检索结果列表
     */
    public List<QueryResult> retrieve(Long datasetId, String query, int limit, float minScore) {
        return retrieve(datasetId, query, limit, minScore, null);
    }

    /**
     * 文本检索（带过滤条件）
     * 
     * @param datasetId 知识库ID
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @param minScore 最小相似度阈值
     * @param filter 额外过滤条件
     * @return 检索结果列表
     */
    public List<QueryResult> retrieve(Long datasetId, String query, int limit, float minScore,
            Map<String, Object> filter) {
        try {
            Dataset dataset = datasetService.getById(datasetId);
            Provider provider = providerService.getProviderByModelId(dataset.getEmbeddingModelId());
            Model model = modelService.getById(dataset.getEmbeddingModelId());

            // 使用EmbeddingClient将查询文本转换为向量
            float[] queryVector = embeddingClient.embed(provider, model, query);

            // 执行向量检索
            return retrieveByVector(datasetId, queryVector, limit, minScore, filter);

        } catch (Exception e) {
            log.error("文本检索失败, datasetId: {}, query: {}", datasetId, query, e);
            throw new RuntimeException("文本检索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 向量检索
     * 
     * @param datasetId 知识库ID
     * @param queryVector 查询向量
     * @param limit 返回结果数量限制
     * @param minScore 最小相似度阈值
     * @param filter 过滤条件
     * @return 检索结果列表
     */
    public List<QueryResult> retrieveByVector(Long datasetId, float[] queryVector, int limit, float minScore,
            Map<String, Object> filter) {
        try {
            // 验证知识库是否存在
            Dataset dataset = datasetService.getById(datasetId);
            if (dataset == null) {
                throw new IllegalArgumentException("知识库不存在, datasetId: " + datasetId);
            }

            // 构建集合名称
            String collectionName = buildCollectionName(datasetId);

            // 检查集合是否存在
            if (!vectorStore.collectionExists(collectionName)) {
                log.warn("知识库对应的向量集合不存在, datasetId: {}, collectionName: {}", datasetId, collectionName);
                return List.of(); // 返回空列表
            }

            // 构建查询过滤条件
            Map<String, Object> queryFilter = buildQueryFilter(datasetId, filter);

            // 构建向量查询
            VectorQuery vectorQuery = VectorQuery.builder().queryVector(queryVector).collectionName(collectionName)
                    .limit(limit).minScore(minScore)
                    .filter(queryFilter).includeVectors(false) // 通常不需要返回向量数据
                    .build();

            // 执行检索
            List<QueryResult> results = vectorStore.similaritySearch(vectorQuery);

            log.info("向量检索完成, datasetId: {}, 结果数量: {}", datasetId, results.size());
            return results;

        } catch (Exception e) {
            log.error("向量检索失败, datasetId: {}", datasetId, e);
            throw new RuntimeException("向量检索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 在多个知识库中检索
     * 
     * @param datasetIds 知识库ID列表
     * @param query 查询文本
     * @param limit 每个知识库返回结果数量限制
     * @param minScore 最小相似度阈值
     * @return 检索结果映射，key为知识库ID，value为检索结果列表
     */
    public Map<Long, List<QueryResult>> retrieveFromMultiple(List<Long> datasetIds, String query, int limit,
            float minScore) {
        Map<Long, List<QueryResult>> results = new HashMap<>();

        for (Long datasetId : datasetIds) {
            try {
                List<QueryResult> datasetResults = retrieve(datasetId, query, limit, minScore);
                results.put(datasetId, datasetResults);
            } catch (Exception e) {
                log.error("在知识库 {} 中检索失败: {}", datasetId, e.getMessage());
                results.put(datasetId, List.of()); // 失败时返回空列表
            }
        }

        return results;
    }

    /**
     * 在指定文档中检索
     * 
     * @param datasetId 知识库ID
     * @param documentId 文档ID
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @param minScore 最小相似度阈值
     * @return 检索结果列表
     */
    public List<QueryResult> retrieveInDocument(Long datasetId, Long documentId, String query, int limit,
            float minScore) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("document_id", documentId);

        return retrieve(datasetId, query, limit, minScore, filter);
    }

    /**
     * 在指定文档的指定分段中检索
     * 
     * @param datasetId 知识库ID
     * @param documentId 文档ID
     * @param segmentIds 分段ID列表
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @param minScore 最小相似度阈值
     * @return 检索结果列表
     */
    public List<QueryResult> retrieveInSegments(Long datasetId, Long documentId, List<String> segmentIds, String query,
            int limit, float minScore) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("document_id", documentId);
        filter.put("segment_id", segmentIds); // 支持多个分段ID

        return retrieve(datasetId, query, limit, minScore, filter);
    }

    /**
     * 混合检索（支持多种检索策略）
     * 
     * @param request 混合检索请求
     * @return 检索结果列表
     */
    public List<QueryResult> hybridRetrieve(HybridRetrievalRequest request) {
        try {
            // 语义检索
            List<QueryResult> semanticResults =
                    retrieve(request.getDatasetId(), request.getQuery(), request.getSemanticLimit(),
                            request.getSemanticMinScore(), request.getFilter());

            // TODO: 可以在这里实现关键词检索、重排序等功能
            // 目前先返回语义检索结果
            return semanticResults;

        } catch (Exception e) {
            log.error("混合检索失败, datasetId: {}, query: {}", request.getDatasetId(), request.getQuery(), e);
            throw new RuntimeException("混合检索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取检索统计信息
     * 
     * @param datasetId 知识库ID
     * @return 统计信息
     */
    public Map<String, Object> getRetrievalStats(Long datasetId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 基本信息
            Dataset dataset = datasetService.getById(datasetId);
            stats.put("datasetExists", dataset != null);

            if (dataset != null) {
                stats.put("datasetName", dataset.getDatasetName());
                stats.put("embeddingModelId", dataset.getEmbeddingModelId());
            }

            // 集合信息
            String collectionName = buildCollectionName(datasetId);
            stats.put("collectionName", collectionName);
            stats.put("collectionExists", vectorStore.collectionExists(collectionName));

            // Provider信息
            try {
                Model model = modelService.getById(dataset.getEmbeddingModelId());
                Provider provider = providerService.getProviderByModelId(dataset.getEmbeddingModelId());
                stats.put("providerName", provider.getProviderName());
                stats.put("embeddingDimension", embeddingClient.getEmbeddingDimension(provider, model));
            } catch (Exception e) {
                stats.put("providerError", e.getMessage());
            }

        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * 清除Provider缓存
     * 
     * @param datasetId 知识库ID（可选，为null时清除所有缓存）
     */
    public void clearProviderCache(Long datasetId) {
        if (datasetId == null) {
            providerCache.clear();
            log.debug("已清除所有Provider缓存");
        } else {
            providerCache.remove(datasetId);
            log.debug("已清除知识库 {} 的Provider缓存", datasetId);
        }
    }

    /**
     * 构建集合名称
     * 
     * @param datasetId 知识库ID
     * @return 集合名称
     */
    private String buildCollectionName(Long datasetId) {
        return config.getCollectionPrefix() + datasetId;
    }

    /**
     * 构建查询过滤条件
     * 
     * @param datasetId 知识库ID
     * @param filter 额外过滤条件
     * @return 合并后的过滤条件
     */
    private Map<String, Object> buildQueryFilter(Long datasetId, Map<String, Object> filter) {
        Map<String, Object> queryFilter = new HashMap<>();
        queryFilter.put("dataset_id", datasetId);

        // 合并额外的过滤条件
        if (filter != null && !filter.isEmpty()) {
            queryFilter.putAll(filter);
        }

        return queryFilter;
    }
}
