package com.yxboot.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.yxboot.ai.vector.AiQueryResult;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.service.ModelService;
import com.yxboot.modules.ai.service.ProviderService;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库向量检索服务（基于 Spring AI VectorStore）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiVectorRetrieverService {

    private final AiVectorStoreService vectorStoreService;
    private final DatasetService datasetService;
    private final ProviderService providerService;
    private final ModelService modelService;

    @Value("${yxboot.ai.retriever.default-limit:10}")
    private int defaultLimit;

    @Value("${yxboot.ai.retriever.default-min-score:0.0}")
    private float defaultMinScore;

    public List<AiQueryResult> retrieve(Long datasetId, String query) {
        return retrieve(datasetId, query, defaultLimit, defaultMinScore);
    }

    public List<AiQueryResult> retrieve(Long datasetId, String query, int limit, float minScore) {
        return retrieve(datasetId, query, limit, minScore, null);
    }

    public List<AiQueryResult> retrieve(Long datasetId, String query, int limit, float minScore,
            Map<String, Object> filter) {
        Dataset dataset = datasetService.getById(datasetId);
        if (dataset == null) {
            throw new IllegalArgumentException("知识库不存在, datasetId: " + datasetId);
        }
        Provider provider = providerService.getProviderByModelId(dataset.getEmbeddingModelId());
        Model model = modelService.getById(dataset.getEmbeddingModelId());
        if (!vectorStoreService.collectionExists(datasetId, dataset.getTenantId())) {
            log.warn("向量集合不存在, datasetId={}, tenantId={}", datasetId, dataset.getTenantId());
            return List.of();
        }
        return vectorStoreService.similaritySearch(
                datasetId, dataset.getTenantId(), provider, model, query, limit, minScore, filter);
    }

    public List<AiQueryResult> retrieveInDocument(Long datasetId, Long documentId, String query, int limit,
            float minScore) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("document_id", documentId);
        return retrieve(datasetId, query, limit, minScore, filter);
    }

    public Map<Long, List<AiQueryResult>> retrieveFromMultiple(List<Long> datasetIds, String query, int limit,
            float minScore) {
        Map<Long, List<AiQueryResult>> results = new HashMap<>();
        for (Long datasetId : datasetIds) {
            try {
                results.put(datasetId, retrieve(datasetId, query, limit, minScore));
            } catch (Exception e) {
                log.error("知识库 {} 检索失败", datasetId, e);
                results.put(datasetId, List.of());
            }
        }
        return results;
    }
}
