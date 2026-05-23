package com.yxboot.modules.dataset.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.yxboot.ai.service.AiVectorStoreService;
import com.yxboot.ai.vector.AiQueryResult;
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

    public List<AiQueryResult> searchInDataset(Long datasetId, String query, int limit, float minScore) {
        return searchInDataset(datasetId, query, limit, minScore, null);
    }

    public List<AiQueryResult> searchInDataset(Long datasetId, String query, int limit, float minScore, Map<String, Object> filter) {
        try {
            Dataset dataset = datasetService.getById(datasetId);
            if (dataset == null) {
                log.error("知识库不存在, datasetId: {}", datasetId);
                throw new RuntimeException("知识库不存在");
            }

            if (!vectorStoreService.collectionExists(datasetId, dataset.getTenantId())) {
                log.warn("知识库对应的向量集合不存在, datasetId: {}, tenantId: {}", datasetId, dataset.getTenantId());
                return List.of();
            }

            datasetService.ensureEmbeddingModelCompatible(dataset);

            List<AiQueryResult> results = vectorStoreService.similaritySearch(
                    datasetId, dataset.getTenantId(), query, limit, minScore, filter);

            log.info("知识库搜索完成, datasetId: {}, query: {}, 结果数量: {}", datasetId, query, results.size());
            return results;

        } catch (Exception e) {
            log.error("知识库搜索失败, datasetId: {}, query: {}", datasetId, query, e);
            throw new RuntimeException("知识库搜索失败: " + e.getMessage());
        }
    }

    public Map<Long, List<AiQueryResult>> searchInMultipleDatasets(List<Long> datasetIds, String query, int limit, float minScore) {
        Map<Long, List<AiQueryResult>> results = new HashMap<>();

        for (Long datasetId : datasetIds) {
            try {
                List<AiQueryResult> datasetResults = searchInDataset(datasetId, query, limit, minScore);
                results.put(datasetId, datasetResults);
            } catch (Exception e) {
                log.error("在知识库 {} 中搜索失败: {}", datasetId, e.getMessage());
                results.put(datasetId, List.of());
            }
        }

        return results;
    }

    public List<AiQueryResult> searchInDocument(Long datasetId, Long documentId, String query, int limit, float minScore) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("document_id", documentId);

        return searchInDataset(datasetId, query, limit, minScore, filter);
    }
}
