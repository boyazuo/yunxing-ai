package com.yxboot.llm.embedding.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 嵌入响应类
 * 封装文本嵌入结果
 * 
 * @author Boya
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 向量结果列表
     */
    @Builder.Default
    private List<EmbeddingResult> data = new ArrayList<>();

    /**
     * 使用的令牌数
     */
    private TokenUsage tokenUsage;

    /**
     * 原始元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 获取第一个嵌入结果
     * 
     * @return 第一个嵌入结果，如果列表为空则返回null
     */
    public EmbeddingResult getFirstResult() {
        return data.isEmpty() ? null : data.get(0);
    }

    /**
     * 获取第一个嵌入向量
     * 
     * @return 第一个嵌入向量，如果列表为空则返回null
     */
    public float[] getFirstEmbedding() {
        EmbeddingResult result = getFirstResult();
        return result == null ? null : result.getEmbedding();
    }

    /**
     * 获取所有嵌入向量
     * 
     * @return 嵌入向量列表
     */
    public List<float[]> getAllEmbeddings() {
        List<float[]> embeddings = new ArrayList<>();
        for (EmbeddingResult result : data) {
            embeddings.add(result.getEmbedding());
        }
        return embeddings;
    }

    /**
     * 嵌入结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddingResult {

        /**
         * 索引
         */
        private int index;

        /**
         * 对象ID
         */
        private String object;

        /**
         * 嵌入向量
         */
        private float[] embedding;
    }

    /**
     * 令牌使用统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {

        /**
         * 输入令牌数
         */
        private int inputTokens;

        /**
         * 总令牌数
         */
        private int totalTokens;

        /**
         * 创建一个令牌使用统计对象
         * 
         * @param inputTokens 输入令牌数
         * @return 令牌使用统计对象
         */
        public static TokenUsage of(int inputTokens) {
            return TokenUsage.builder()
                    .inputTokens(inputTokens)
                    .totalTokens(inputTokens)
                    .build();
        }
    }
}