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
@Builder
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
}
