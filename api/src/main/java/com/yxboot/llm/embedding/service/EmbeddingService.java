package com.yxboot.llm.embedding.service;

import java.util.List;
import java.util.Map;

import com.yxboot.llm.document.Document;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.llm.embedding.storage.query.QueryResult;
import com.yxboot.llm.embedding.storage.query.VectorQuery;

/**
 * 文本嵌入服务接口，提供向量化和检索功能
 */
public interface EmbeddingService {

    /**
     * 将文本转换为向量
     * 
     * @param text 输入文本
     * @return 向量表示
     */
    float[] embedText(String text);

    /**
     * 批量将文本转换为向量
     * 
     * @param texts 输入文本列表
     * @return 向量表示列表
     */
    List<float[]> embedTexts(List<String> texts);

    /**
     * 将文档块添加到向量存储
     * 
     * @param chunk 文档块
     * @return 是否添加成功
     */
    boolean addDocumentSegment(DocumentSegment segment);

    /**
     * 批量将文档块添加到向量存储
     * 
     * @param chunks 文档块列表
     * @return 添加成功的数量
     */
    int addDocumentSegments(List<DocumentSegment> segments);

    /**
     * 将文档处理并添加到向量存储
     * 
     * @param document 文档
     * @return 添加的文档块数量
     */
    int addDocument(Document document);

    /**
     * 批量将文档处理并添加到向量存储
     * 
     * @param documents 文档列表
     * @return 添加的文档块数量
     */
    int addDocuments(List<Document> documents);

    /**
     * 相似度搜索
     * 
     * @param query 查询文本
     * @param limit 返回结果数量
     * @return 查询结果
     */
    List<QueryResult> similaritySearch(String query, int limit);

    /**
     * 相似度搜索（带过滤条件）
     * 
     * @param query  查询文本
     * @param filter 过滤条件
     * @param limit  返回结果数量
     * @return 查询结果
     */
    List<QueryResult> similaritySearch(String query, Map<String, Object> filter, int limit);

    /**
     * 高级相似度搜索
     * 
     * @param query 查询参数
     * @return 查询结果
     */
    List<QueryResult> similaritySearch(VectorQuery query);

    /**
     * 删除向量
     * 
     * @param id 向量ID
     * @return 是否删除成功
     */
    boolean deleteVector(String id);

    /**
     * 删除多个向量
     * 
     * @param ids 向量ID列表
     * @return 删除成功的数量
     */
    int deleteVectors(List<String> ids);

    /**
     * 根据过滤条件删除向量
     * 
     * @param filter 过滤条件
     * @return 删除成功的数量
     */
    int deleteVectorsByFilter(Map<String, Object> filter);
}