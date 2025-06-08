package com.yxboot.llm.client.vector;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 混合检索请求 支持多种检索策略的组合
 * 
 * @author Boya
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HybridRetrievalRequest {

    /**
     * 知识库ID
     */
    private Long datasetId;

    /**
     * 查询文本
     */
    private String query;

    /**
     * 语义检索结果数量限制
     */
    @Builder.Default
    private int semanticLimit = 10;

    /**
     * 语义检索最小相似度阈值
     */
    @Builder.Default
    private float semanticMinScore = 0.0f;

    /**
     * 关键词检索结果数量限制
     */
    @Builder.Default
    private int keywordLimit = 10;

    /**
     * 关键词检索最小相似度阈值
     */
    @Builder.Default
    private float keywordMinScore = 0.0f;

    /**
     * 语义检索权重
     */
    @Builder.Default
    private float semanticWeight = 0.7f;

    /**
     * 关键词检索权重
     */
    @Builder.Default
    private float keywordWeight = 0.3f;

    /**
     * 最终返回结果数量限制
     */
    @Builder.Default
    private int finalLimit = 10;

    /**
     * 重排序算法
     */
    @Builder.Default
    private String rerankAlgorithm = "rrf"; // reciprocal rank fusion

    /**
     * 过滤条件
     */
    private Map<String, Object> filter;

    /**
     * 是否启用重排序
     */
    @Builder.Default
    private boolean enableRerank = true;

    /**
     * 额外选项
     */
    private Map<String, Object> options;
}
