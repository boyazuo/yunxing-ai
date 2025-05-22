package com.yxboot.llm.embedding.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yxboot.llm.document.Document;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.llm.document.service.DocumentProcessorService;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.embedding.service.EmbeddingService;
import com.yxboot.llm.embedding.storage.VectorStore;
import com.yxboot.llm.embedding.storage.query.QueryResult;
import com.yxboot.llm.embedding.storage.query.VectorQuery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 嵌入服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final DocumentProcessorService documentProcessorService;

    @Override
    public float[] embedText(String text) {
        return embeddingModel.embed(text);
    }

    @Override
    public List<float[]> embedTexts(List<String> texts) {
        return embeddingModel.embedAll(texts);
    }

    @Override
    public boolean addDocumentSegment(DocumentSegment segment) {
        // 生成向量
        float[] vector = embeddingModel.embed(segment.getContent());

        // 添加到向量存储
        return vectorStore.addVector(
                segment.getId(),
                vector,
                segment.getMetadata(),
                segment.getContent());
    }

    @Override
    public int addDocumentSegments(List<DocumentSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }

        // 提取所有文本内容
        List<String> texts = new ArrayList<>();
        for (DocumentSegment segment : segments) {
            texts.add(segment.getContent());
        }

        // 批量生成向量
        List<float[]> vectors = embeddingModel.embedAll(texts);

        // 批量添加到向量存储
        return vectorStore.addDocumentSegments(segments, vectors);
    }

    @Override
    public int addDocument(Document document) {
        if (document == null) {
            return 0;
        }

        // 分割文档
        List<DocumentSegment> segments = documentProcessorService.splitDocument(document);

        // 添加文档块
        return addDocumentSegments(segments);
    }

    @Override
    public int addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }

        int totalChunks = 0;
        for (Document document : documents) {
            totalChunks += addDocument(document);
        }

        return totalChunks;
    }

    @Override
    public List<QueryResult> similaritySearch(String query, int limit) {
        return similaritySearch(query, null, limit);
    }

    @Override
    public List<QueryResult> similaritySearch(String query, Map<String, Object> filter, int limit) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyList();
        }

        // 创建查询参数
        VectorQuery vectorQuery = VectorQuery.builder()
                .queryText(query)
                .filter(filter)
                .limit(limit)
                .build();

        // 执行查询
        return vectorStore.similaritySearch(vectorQuery);
    }

    @Override
    public List<QueryResult> similaritySearch(VectorQuery query) {
        if (query == null) {
            return Collections.emptyList();
        }

        return vectorStore.similaritySearch(query);
    }

    @Override
    public boolean deleteVector(String id) {
        return vectorStore.deleteVector(id);
    }

    @Override
    public int deleteVectors(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        return vectorStore.deleteVectors(ids);
    }

    @Override
    public int deleteVectorsByFilter(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return 0;
        }

        return vectorStore.deleteVectorsByFilter(filter);
    }
}