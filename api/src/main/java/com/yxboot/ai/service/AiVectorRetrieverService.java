package com.yxboot.ai.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.yxboot.ai.config.AiProperties;
import com.yxboot.ai.vector.AiQueryResult;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import com.yxboot.modules.dataset.enums.SegmentType;
import com.yxboot.modules.dataset.service.DatasetDocumentSegmentService;
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
    private final DatasetDocumentSegmentService segmentService;
    private final AiProperties aiProperties;

    public List<AiQueryResult> retrieve(Long datasetId, String query) {
        AiProperties.RetrieverConfig retriever = aiProperties.getRetriever();
        return retrieve(datasetId, query, retriever.getDefaultLimit(), retriever.getDefaultMinScore());
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
        if (!vectorStoreService.collectionExists(datasetId, dataset.getTenantId())) {
            log.warn("向量集合不存在, datasetId={}, tenantId={}", datasetId, dataset.getTenantId());
            return List.of();
        }
        datasetService.ensureEmbeddingModelCompatible(dataset);
        List<AiQueryResult> childResults = vectorStoreService.similaritySearch(
                datasetId, dataset.getTenantId(), query, limit, minScore, filter);
        return expandToParentSegments(childResults);
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

    /**
     * 子块命中后扩展为父块内容，同一父块去重并保留最高相似度。
     */
    private List<AiQueryResult> expandToParentSegments(List<AiQueryResult> childResults) {
        if (childResults == null || childResults.isEmpty()) {
            return List.of();
        }

        List<Long> segmentIds = childResults.stream()
                .map(this::extractSegmentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (segmentIds.isEmpty()) {
            return childResults;
        }

        Map<Long, DatasetDocumentSegment> segmentMap = segmentService.listByIds(segmentIds).stream()
                .collect(Collectors.toMap(DatasetDocumentSegment::getSegmentId, s -> s));

        List<Long> parentIds = new ArrayList<>();
        for (AiQueryResult result : childResults) {
            Long segmentId = extractSegmentId(result);
            if (segmentId == null) {
                continue;
            }
            DatasetDocumentSegment segment = segmentMap.get(segmentId);
            if (segment == null) {
                continue;
            }
            if (segment.getSegmentType() != null && segment.getSegmentType() == SegmentType.CHILD
                    && segment.getParentSegmentId() != null) {
                parentIds.add(segment.getParentSegmentId());
            }
        }

        Map<Long, DatasetDocumentSegment> parentMap = parentIds.isEmpty() ? Map.of()
                : segmentService.listByIds(parentIds.stream().distinct().collect(Collectors.toList())).stream()
                        .collect(Collectors.toMap(DatasetDocumentSegment::getSegmentId, s -> s));

        Map<String, AiQueryResult> deduped = new LinkedHashMap<>();
        for (AiQueryResult result : childResults) {
            Long segmentId = extractSegmentId(result);
            DatasetDocumentSegment segment = segmentId != null ? segmentMap.get(segmentId) : null;
            AiQueryResult expanded = buildExpandedResult(result, segment, parentMap);
            String dedupeKey = expanded.getId() != null ? expanded.getId() : String.valueOf(segmentId);
            AiQueryResult existing = deduped.get(dedupeKey);
            if (existing == null || expanded.getScore() > existing.getScore()) {
                deduped.put(dedupeKey, expanded);
            }
        }

        return new ArrayList<>(deduped.values());
    }

    private AiQueryResult buildExpandedResult(AiQueryResult result, DatasetDocumentSegment segment,
            Map<Long, DatasetDocumentSegment> parentMap) {
        if (segment == null) {
            return result;
        }

        Integer segmentType = segment.getSegmentType();
        if (segmentType == null || segmentType == SegmentType.NORMAL) {
            return result;
        }

        if (segmentType == SegmentType.CHILD && segment.getParentSegmentId() != null) {
            DatasetDocumentSegment parent = parentMap.get(segment.getParentSegmentId());
            if (parent != null) {
                return AiQueryResult.builder()
                        .id(String.valueOf(parent.getSegmentId()))
                        .text(parent.getContent())
                        .score(result.getScore())
                        .metadata(result.getMetadata())
                        .hitChildSegmentId(segment.getSegmentId())
                        .hitChildContent(result.getText())
                        .build();
            }
        }

        return result;
    }

    private Long extractSegmentId(AiQueryResult result) {
        if (result.getMetadata() == null) {
            return null;
        }
        Object segmentId = result.getMetadata().get("segment_id");
        if (segmentId instanceof Number number) {
            return number.longValue();
        }
        if (segmentId instanceof String str && !str.isBlank()) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
