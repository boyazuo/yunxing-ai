package com.yxboot.llm.embedding.storage;

import java.util.List;
import java.util.Map;

import com.yxboot.llm.document.DocumentChunk;
import com.yxboot.llm.embedding.storage.query.QueryResult;
import com.yxboot.llm.embedding.storage.query.VectorQuery;

/**
 * 向量存储接口，用于存储和检索向量数据
 */
public interface VectorStore {

    /**
     * 添加单个向量到存储
     *
     * @param id       向量ID
     * @param vector   向量数据
     * @param metadata 元数据
     * @param text     原始文本
     * @return 是否添加成功
     */
    boolean addVector(String id, float[] vector, Map<String, Object> metadata, String text);

    /**
     * 批量添加向量到存储
     *
     * @param ids          向量ID列表
     * @param vectors      向量数据列表
     * @param metadataList 元数据列表
     * @param texts        原始文本列表
     * @return 添加成功的数量
     */
    int addVectors(List<String> ids, List<float[]> vectors, List<Map<String, Object>> metadataList, List<String> texts);

    /**
     * 使用向量添加文档块
     *
     * @param chunks  文档块列表
     * @param vectors 向量列表
     * @return 添加成功的数量
     */
    int addDocumentChunks(List<DocumentChunk> chunks, List<float[]> vectors);

    /**
     * 根据ID删除向量
     *
     * @param id 向量ID
     * @return 是否删除成功
     */
    boolean deleteVector(String id);

    /**
     * 批量删除向量
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

    /**
     * 相似度搜索
     *
     * @param query 查询参数
     * @return 查询结果
     */
    List<QueryResult> similaritySearch(VectorQuery query);

    /**
     * 创建集合（如果不存在）
     *
     * @param collectionName 集合名称
     * @param dimension      向量维度
     * @return 是否创建成功
     */
    boolean createCollection(String collectionName, int dimension);

    /**
     * 删除集合
     *
     * @param collectionName 集合名称
     * @return 是否删除成功
     */
    boolean deleteCollection(String collectionName);

    /**
     * 检查集合是否存在
     *
     * @param collectionName 集合名称
     * @return 是否存在
     */
    boolean collectionExists(String collectionName);
}