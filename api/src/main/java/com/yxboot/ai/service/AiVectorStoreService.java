package com.yxboot.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import com.yxboot.ai.config.AiProperties;
import com.yxboot.ai.registry.VectorStoreRegistry;
import com.yxboot.ai.vector.AiQueryResult;
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

    private static final int DEFAULT_EMBEDDING_BATCH_SIZE = 16;

    private final VectorStoreRegistry vectorStoreRegistry;
    private final AiProperties aiProperties;

    public String getCollectionName(Long datasetId, Long tenantId) {
        return vectorStoreRegistry.buildCollectionName(datasetId, tenantId);
    }

    public boolean collectionExists(Long datasetId, Long tenantId) {
        return vectorStoreRegistry.collectionExists(datasetId, tenantId);
    }

    public boolean deleteCollection(Long datasetId, Long tenantId) {
        return vectorStoreRegistry.deleteCollection(datasetId, tenantId);
    }

    public void ensureCollectionExists(Long datasetId, Long tenantId) {
        vectorStoreRegistry.ensureCollectionExists(datasetId, tenantId);
    }

    public List<AiQueryResult> similaritySearch(Long datasetId, Long tenantId, String queryText, int limit,
            float minScore, Map<String, Object> filter) {
        VectorStore vectorStore = vectorStoreRegistry.getOrCreate(datasetId, tenantId);
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(queryText)
                .topK(limit)
                .similarityThreshold(minScore);
        builder.filterExpression(buildFilter(datasetId, filter));
        List<Document> documents = vectorStore.similaritySearch(builder.build());
        return documents.stream().map(this::toQueryResult).toList();
    }

    public int batchCreateSegmentVectors(List<DatasetDocumentSegment> segments, Long datasetId) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }
        Long tenantId = segments.get(0).getTenantId();
        VectorStore vectorStore = vectorStoreRegistry.getOrCreate(datasetId, tenantId);
        List<Document> documents = segments.stream()
                .filter(s -> s.getContent() != null && !s.getContent().isBlank())
                .filter(s -> s.getVectorId() != null && !s.getVectorId().isBlank())
                .map(this::toDocument)
                .toList();
        if (documents.isEmpty()) {
            return 0;
        }
        int batchSize = resolveEmbeddingBatchSize();
        int total = 0;
        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, end);
            vectorStore.add(batch);
            total += batch.size();
            log.info("向量化批次完成, datasetId={}, tenantId={}, progress={}/{}", datasetId, tenantId, total,
                    documents.size());
        }
        log.info("批量向量化完成, datasetId={}, tenantId={}, count={}", datasetId, tenantId, total);
        return total;
    }

    private int resolveEmbeddingBatchSize() {
        Integer configured = aiProperties.getEmbedding().getBatchSize();
        if (configured == null || configured <= 0) {
            return DEFAULT_EMBEDDING_BATCH_SIZE;
        }
        return configured;
    }

    public boolean createSegmentVector(DatasetDocumentSegment segment) {
        return batchCreateSegmentVectors(List.of(segment), segment.getDatasetId()) == 1;
    }

    public boolean updateSegmentVector(DatasetDocumentSegment segment) {
        deleteSegmentVector(segment);
        return createSegmentVector(segment);
    }

    public boolean deleteSegmentVector(DatasetDocumentSegment segment) {
        try {
            VectorStore vectorStore = vectorStoreRegistry.getOrCreate(segment.getDatasetId(), segment.getTenantId());
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
            VectorStore vectorStore = vectorStoreRegistry.getOrCreate(datasetId, tenantId);
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
            VectorStore vectorStore = vectorStoreRegistry.getOrCreate(datasetId, tenantId);
            vectorStore.delete(buildFilter(datasetId, filter));
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

    /**
     * 使用 FilterExpressionBuilder 构建过滤条件。
     * Snowflake ID 在 Qdrant payload 中以字符串存储，过滤值需转为字符串才能命中。
     */
    private Filter.Expression buildFilter(Long datasetId, Map<String, Object> filter) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        FilterExpressionBuilder.Op expression = builder.eq("dataset_id", toMetadataFilterValue(datasetId));
        if (filter != null) {
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                if ("dataset_id".equals(entry.getKey())) {
                    continue;
                }
                expression = builder.and(expression,
                        builder.eq(entry.getKey(), toMetadataFilterValue(entry.getValue())));
            }
        }
        return expression.build();
    }

    private Object toMetadataFilterValue(Object value) {
        if (value instanceof Number number) {
            return number.toString();
        }
        return value;
    }
}
