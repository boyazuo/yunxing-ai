package com.yxboot.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "yxboot.llm.vector")
public class VectorProperties {

    private QdrantProperties qdrant = new QdrantProperties();

    @Data
    public static class QdrantProperties {
        private String host = "localhost";
        private int port = 6333;
        private int grpcPort = 6334;
        private boolean https = false;
        private String apiKey;
        private String defaultCollectionName = "documents";
    }

}
