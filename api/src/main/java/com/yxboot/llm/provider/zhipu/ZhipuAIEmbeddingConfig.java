package com.yxboot.llm.provider.zhipu;

import com.yxboot.llm.embedding.config.EmbeddingConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智谱AI嵌入模型配置
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ZhipuAIEmbeddingConfig implements EmbeddingConfig {

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 模型名称
     */
    @Builder.Default
    private String modelName = "embedding-3";

    /**
     * API基础URL
     */
    @Builder.Default
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/embeddings";

    /**
     * 向量维度
     */
    @Builder.Default
    private int embeddingDimension = 2048;

    /**
     * 批处理大小
     */
    @Builder.Default
    private int batchSize = 32;

    /**
     * 使用默认API密钥创建配置
     * 
     * @param apiKey API密钥
     * @return 智谱AI配置
     */
    public static ZhipuAIEmbeddingConfig of(String apiKey) {
        return ZhipuAIEmbeddingConfig.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * 创建带自定义模型的配置
     * 
     * @param apiKey API密钥
     * @param modelName 模型名称
     * @return 智谱AI配置
     */
    public static ZhipuAIEmbeddingConfig of(String apiKey, String modelName) {
        return ZhipuAIEmbeddingConfig.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    /**
     * 设置API密钥
     * 
     * @param apiKey API密钥
     * @return 新的配置实例
     */
    public ZhipuAIEmbeddingConfig withApiKey(String apiKey) {
        return this.toBuilder().apiKey(apiKey).build();
    }

    /**
     * 设置模型名称
     * 
     * @param modelName 模型名称
     * @return 新的配置实例
     */
    public ZhipuAIEmbeddingConfig withModelName(String modelName) {
        return this.toBuilder().modelName(modelName).build();
    }

    /**
     * 设置向量维度
     * 
     * @param embeddingDimension 向量维度
     * @return 新的配置实例
     */
    public ZhipuAIEmbeddingConfig withEmbeddingDimension(int embeddingDimension) {
        return this.toBuilder().embeddingDimension(embeddingDimension).build();
    }

    /**
     * 设置批处理大小
     * 
     * @param batchSize 批处理大小
     * @return 新的配置实例
     */
    public ZhipuAIEmbeddingConfig withBatchSize(int batchSize) {
        return this.toBuilder().batchSize(batchSize).build();
    }

    /**
     * 设置基础URL
     * 
     * @param baseUrl 基础URL
     * @return 新的配置实例
     */
    public ZhipuAIEmbeddingConfig withBaseUrl(String baseUrl) {
        return this.toBuilder().baseUrl(baseUrl).build();
    }
}
