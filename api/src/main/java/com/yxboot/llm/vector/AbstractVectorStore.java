package com.yxboot.llm.vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.vector.query.QueryResult;
import com.yxboot.llm.vector.query.VectorQuery;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象向量存储基类，提供通用功能
 */
@Slf4j
public abstract class AbstractVectorStore implements VectorStore {

    /**
     * 默认集合名称
     */
    @Getter
    protected final String defaultCollectionName;

    /**
     * 嵌入模型
     */
    @Getter
    protected final EmbeddingModel embeddingModel;

    /**
     * 构造函数
     *
     * @param defaultCollectionName 默认集合名称
     * @param embeddingModel        嵌入模型
     */
    public AbstractVectorStore(String defaultCollectionName, EmbeddingModel embeddingModel) {
        this.defaultCollectionName = defaultCollectionName;
        this.embeddingModel = embeddingModel;
    }

    /**
     * 使用向量添加文档块的默认实现
     */
    @Override
    public int addDocumentSegments(String collectionName, List<DocumentSegment> segments, List<float[]> vectors) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }

        if (vectors == null || vectors.size() != segments.size()) {
            throw new IllegalArgumentException("向量列表大小必须与文档块列表大小相同");
        }

        List<String> ids = segments.stream()
                .map(segment -> segment.getId() != null ? segment.getId() : UUID.randomUUID().toString())
                .collect(Collectors.toList());

        List<Map<String, Object>> metadataList = segments.stream()
                .map(DocumentSegment::getMetadata)
                .collect(Collectors.toList());

        List<String> texts = segments.stream()
                .map(segment -> segment.getTitle() + " " + segment.getContent())
                .collect(Collectors.toList());

        // 更新segment的ID，确保每个segment都有ID
        IntStream.range(0, segments.size())
                .forEach(i -> segments.get(i).setId(ids.get(i)));

        return addVectors(collectionName, ids, vectors, metadataList, texts);
    }

    /**
     * 相似度搜索，支持使用文本自动转换为向量查询
     */
    @Override
    public List<QueryResult> similaritySearch(VectorQuery query) {
        // 如果提供了查询文本但没有提供查询向量，使用嵌入模型将文本转换为向量
        if (query.getQueryVector() == null && query.getQueryText() != null) {
            float[] queryVector = embeddingModel.embed(query.getQueryText());
            query.setQueryVector(queryVector);
        }

        // 如果没有指定集合名称，使用默认集合
        if (query.getCollectionName() == null || query.getCollectionName().isEmpty()) {
            query.setCollectionName(defaultCollectionName);
        }

        // 执行具体的相似度搜索
        return doSimilaritySearch(query);
    }

    /**
     * 具体的相似度搜索实现，由子类实现
     *
     * @param query 查询参数
     * @return 查询结果
     */
    protected abstract List<QueryResult> doSimilaritySearch(VectorQuery query);
}