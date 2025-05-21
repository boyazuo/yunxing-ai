package com.yxboot.llm.embedding.storage.query;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量查询结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResult {

    /**
     * 结果ID
     */
    private String id;

    /**
     * 原始文本内容
     */
    private String text;

    /**
     * 相似度分数
     */
    private float score;

    /**
     * 向量数据
     */
    private float[] vector;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}