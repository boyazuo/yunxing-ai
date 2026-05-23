package com.yxboot.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import com.yxboot.ai.registry.VectorStoreRegistry;
import com.yxboot.ai.vector.AiQueryResult;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库向量存储服务（基于 Spring AI VectorStore）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiVectorStoreService {

    private final VectorStoreRegistry vectorStoreRegistry;

    public String getCollectionName(Long datasetId, Long tenantId) {
        return vectorStoreRegistry.buildCollectionName(datasetId, tenantId);
    }

    public boolean collectionExists(Long datasetId, Long tenantId) {
        return vectorStoreRegistry.collectionExists(datasetId, tenantId);
    }

    public boolean deleteCollection(Long datasetId, Long tenantId) {
        return vectorStoreRegistry.deleteCollection(datasetId, tenantId);
    }

    public List<AiQueryResult> similaritySearch(Long datasetId, Long tenantId, Provider provider, Model model,
            String queryText, int limit, float minScore, Map<String, Object> filter) {
        VectorStore vectorStore = vectorStoreRegistry.getOrCreate(datasetId, tenantId, provider, model);
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(queryText)
                .topK(limit)
                .similarityThreshold(minScore);
        String filterExpression = buildFilterExpression(datasetId, filter);
        if (filterExpression != null) {
            builder.filterExpression(filterExpression);
        }
        List<Document> documents = vectorStore.similaritySearch(builder.build());
        return documents.stream().map(this::toQueryResult).toList();
    }

    public int batchCreateSegmentVectors(List<DatasetDocumentSegment> segments, Long datasetId, Provider provider,
            Model model) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }
        Long tenantId = segments.get(0).getTenantId();
        VectorStore vectorStore = vectorStoreRegistry.getOrCreate(datasetId, tenantId, provider, model);
        List<Document> documents = segments.stream()
                .filter(s -> s.getContent() != null && !s.getContent().isBlank())
                .map(this::toDocument)
                .toList();
        if (documents.isEmpty()) {
            return 0;
        }
        vectorStore.add(documents);
        log.info("批量向量化完成, datasetId={}, tenantId={}, count={}", datasetId, tenantId, documents.size());
        return documents.size();
    }

    public boolean createSegmentVector(DatasetDocumentSegment segment, Provider provider, Model model) {
        return batchCreateSegmentVectors(List.of(segment), segment.getDatasetId(), provider, model) == 1;
    }

    public boolean updateSegmentVector(DatasetDocumentSegment segment, Provider provider, Model model) {
        deleteSegmentVector(segment);
        return createSegmentVector(segment, provider, model);
    }

    public boolean deleteSegmentVector(DatasetDocumentSegment segment) {
        try {
            VectorStore vectorStore = vectorStoreRegistry.getOrCreate(
                    segment.getDatasetId(), segment.getTenantId(),
                    dummyProvider(segment.getTenantId()), dummyModel());
            if (segment.getVectorId() != null) {
                vectorStore.delete(List.of(segment.getVectorId()));
            }
            return true;
        } catch (Exception e) {
            log.error("删除分段向量失败, segmentId={}", segment.getSegmentId(), e);
            return false;
        }
    }

    public int batchDeleteSegmentVectors(List<DatasetDocumentSegment> segments, Long datasetId) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }
        Long tenantId = segments.get(0).getTenantId();
        try {
            VectorStore vectorStore = vectorStoreRegistry.getOrCreate(datasetId, tenantId,
                    dummyProvider(tenantId), dummyModel());
            List<String> ids = segments.stream()
                    .map(DatasetDocumentSegment::getVectorId)
                    .filter(id -> id != null && !id.isBlank())
                    .toList();
            if (ids.isEmpty()) {
                return 0;
            }
            vectorStore.delete(ids);
            return ids.size();
        } catch (Exception e) {
            log.error("批量删除分段向量失败, datasetId={}", datasetId, e);
            return 0;
        }
    }

    public int deleteDocumentVectors(Long documentId, Long datasetId, Long tenantId) {
        return deleteVectorsByFilter(datasetId, tenantId, Map.of("document_id", documentId));
    }

    public int deleteVectorsByFilter(Long datasetId, Long tenantId, Map<String, Object> filter) {
        try {
            if (!vectorStoreRegistry.collectionExists(datasetId, tenantId)) {
                return 0;
            }
            VectorStore vectorStore = vectorStoreRegistry.getOrCreate(datasetId, tenantId,
                    dummyProvider(tenantId), dummyModel());
            String expression = buildFilterExpression(datasetId, filter);
            if (expression != null) {
                vectorStore.delete(expression);
            }
            return 0;
        } catch (Exception e) {
            log.error("按条件删除向量失败, datasetId={}, tenantId={}", datasetId, tenantId, e);
            return 0;
        }
    }

    private Document toDocument(DatasetDocumentSegment segment) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("document_id", segment.getDocumentId());
        metadata.put("dataset_id", segment.getDatasetId());
        metadata.put("tenant_id", segment.getTenantId());
        metadata.put("title", segment.getTitle());
        metadata.put("segment_id", segment.getSegmentId());
        String id = segment.getVectorId() != null ? segment.getVectorId() : String.valueOf(segment.getSegmentId());
        return new Document(id, segment.getContent(), metadata);
    }

    private AiQueryResult toQueryResult(Document doc) {
        float score = 0f;
        Object distance = doc.getMetadata().get("distance");
        if (distance instanceof Number number) {
            score = 1f - number.floatValue();
        }
        return AiQueryResult.builder()
                .id(doc.getId())
                .text(doc.getText())
                .score(score)
                .metadata(doc.getMetadata())
                .build();
    }

    private String buildFilterExpression(Long datasetId, Map<String, Object> filter) {
        StringBuilder sb = new StringBuilder("dataset_id == ").append(datasetId);
        if (filter != null) {
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                if ("dataset_id".equals(entry.getKey())) {
                    continue;
                }
                sb.append(" && ").append(entry.getKey()).append(" == ");
                Object value = entry.getValue();
                if (value instanceof String) {
                    sb.append("'").append(value).append("'");
                } else {
                    sb.append(value);
                }
            }
        }
        return sb.toString();
    }

    private Provider dummyProvider(Long tenantId) {
        Provider p = new Provider();
        p.setProviderName("zhipu");
        p.setApiKey("placeholder");
        p.setTenantId(tenantId);
        return p;
    }

    private Model dummyModel() {
        Model m = new Model();
        m.setModelName("embedding-3");
        return m;
    }
}
