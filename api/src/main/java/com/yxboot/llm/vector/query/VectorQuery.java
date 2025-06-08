package com.yxboot.llm.vector.query;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorQuery {
    
    /**
     * 查询向量
     */
    private float[] queryVector;
    
    /**
     * 查询文本（用于先转换为向量再查询）
     */
    private String queryText;
    
    /**
     * 集合名称
     */
    private String collectionName;
    
    /**
     * 返回结果数量
     */
    @Builder.Default
    private int limit = 10;
    
    /**
     * 最小相似度阈值
     */
    @Builder.Default
    private float minScore = 0.0f;
    
    /**
     * 元数据过滤条件
     */
    private Map<String, Object> filter;
    
    /**
     * 是否包含向量数据在结果中
     */
    @Builder.Default
    private boolean includeVectors = false;
} 