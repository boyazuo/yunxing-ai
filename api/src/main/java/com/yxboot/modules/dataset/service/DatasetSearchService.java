package com.yxboot.modules.dataset.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.yxboot.ai.service.AiVectorStoreService;
import com.yxboot.ai.vector.AiQueryResult;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.service.ModelService;
import com.yxboot.modules.ai.service.ProviderService;
import com.yxboot.modules.dataset.entity.Dataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库搜索服务
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetSearchService {

    private final AiVectorStoreService vectorStoreService;
    private final DatasetService datasetService;
    private final ProviderService providerService;
    private final ModelService modelService;

    /**
     * 在指定知识库中搜索相关内容
     * 
     * @param datasetId 知识库ID
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @param minScore 最小相似度阈值
     * @return 搜索结果列表
     */
    public List<AiQueryResult> searchInDataset(Long datasetId, String query, int limit, float minScore) {
        return searchInDataset(datasetId, query, limit, minScore, null);
    }

    /**
     * 在指定知识库中搜索相关内容（带过滤条件）
     * 
     * @param datasetId 知识库ID
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @param minScore 最小相似度阈值
     * @param filter 额外过滤条件
     * @return 搜索结果列表
     */
    public List<AiQueryResult> searchInDataset(Long datasetId, String query, int limit, float minScore, Map<String, Object> filter) {
        try {
            // 获取知识库信息
            Dataset dataset = datasetService.getById(datasetId);
            if (dataset == null) {
                log.error("知识库不存在, datasetId: {}", datasetId);
                throw new RuntimeException("知识库不存在");
            }

            // 获取提供商和模型信息
            Provider provider = providerService.getProviderByModelId(dataset.getEmbeddingModelId());
            if (provider == null) {
                log.error("获取提供商失败, embeddingModelId: {}", dataset.getEmbeddingModelId());
                throw new RuntimeException("获取提供商失败");
            }

            Model model = modelService.getById(dataset.getEmbeddingModelId());
            if (model == null) {
                log.error("获取模型失败, embeddingModelId: {}", dataset.getEmbeddingModelId());
                throw new RuntimeException("获取模型失败");
            }

            // 检查集合是否存在
            if (!vectorStoreService.collectionExists(datasetId, dataset.getTenantId())) {
                log.warn("知识库对应的向量集合不存在, datasetId: {}, tenantId: {}", datasetId, dataset.getTenantId());
                return List.of(); // 返回空列表
            }

            List<AiQueryResult> results = vectorStoreService.similaritySearch(
                    datasetId, dataset.getTenantId(), provider, model, query, limit, minScore, filter);

            log.info("知识库搜索完成, datasetId: {}, query: {}, 结果数量: {}", datasetId, query, results.size());
            return results;

        } catch (Exception e) {
            log.error("知识库搜索失败, datasetId: {}, query: {}", datasetId, query, e);
            throw new RuntimeException("知识库搜索失败: " + e.getMessage());
        }
    }

    /**
     * 在多个知识库中搜索相关内容
     * 
     * @param datasetIds 知识库ID列表
     * @param query 查询文本
     * @param limit 每个知识库返回结果数量限制
     * @param minScore 最小相似度阈值
     * @return 搜索结果映射，key为知识库ID，value为搜索结果列表
     */
    public Map<Long, List<AiQueryResult>> searchInMultipleDatasets(List<Long> datasetIds, String query, int limit, float minScore) {
        Map<Long, List<AiQueryResult>> results = new HashMap<>();

        for (Long datasetId : datasetIds) {
            try {
                List<AiQueryResult> datasetResults = searchInDataset(datasetId, query, limit, minScore);
                results.put(datasetId, datasetResults);
            } catch (Exception e) {
                log.error("在知识库 {} 中搜索失败: {}", datasetId, e.getMessage());
                results.put(datasetId, List.of()); // 失败时返回空列表
            }
        }

        return results;
    }

    /**
     * 在指定文档中搜索相关内容
     * 
     * @param datasetId 知识库ID
     * @param documentId 文档ID
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @param minScore 最小相似度阈值
     * @return 搜索结果列表
     */
    public List<AiQueryResult> searchInDocument(Long datasetId, Long documentId, String query, int limit, float minScore) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("document_id", documentId);

        return searchInDataset(datasetId, query, limit, minScore, filter);
    }
}
