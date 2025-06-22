package com.yxboot.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "yxboot.llm.embedding")
public class EmbeddingProperties {

    private String defaultProvider = "zhipuai";
    private ZhipuAIEmbeddingProperties zhipuai = new ZhipuAIEmbeddingProperties();

    @Data
    public static class ZhipuAIEmbeddingProperties {
        private String apiKey;
        private String modelName = "embedding-3";
        private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/embeddings";
        private int embeddingDimension = 2048;
        private int batchSize = 32;
    }

}
