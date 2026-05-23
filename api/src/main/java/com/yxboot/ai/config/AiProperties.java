package com.yxboot.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "yxboot.ai")
public class AiProperties {

    private ChatConfig chat = new ChatConfig();
    private EmbeddingConfig embedding = new EmbeddingConfig();
    private RetrieverConfig retriever = new RetrieverConfig();

    @Data
    public static class ChatConfig {
        private String provider = "zhipuai";
        private String apiKey;
        private String model = "glm-4-flash";
        private Double temperature = 0.7;
        private Double topP = 0.95;
        private Integer maxTokens = 4096;
    }

    @Data
    public static class EmbeddingConfig {
        private String provider = "zhipuai";
        private String apiKey;
        private String model = "embedding-3";
        private Integer dimensions = 2048;
        /** Ollama 专用：服务地址，默认 http://localhost:11434 */
        private String baseUrl = "http://localhost:11434";

        /** 返回向量模型标识，格式 provider:model */
        public String toModelKey() {
            return provider + ":" + model;
        }
    }

    /** 当前系统配置的向量模型标识 */
    public String getEmbeddingModelKey() {
        return embedding.toModelKey();
    }

    @Data
    public static class RetrieverConfig {
        private int defaultLimit = 10;
        private float defaultMinScore = 0.0f;
    }
}
