package com.yxboot.llm.embedding.storage.qdrant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * QDrant配置类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QdrantConfig {

    /**
     * 服务器地址
     */
    @Builder.Default
    private String host = "127.0.0.1";

    /**
     * 服务器端口
     */
    @Builder.Default
    private int port = 6333;

    /**
     * gRPC端口
     */
    @Builder.Default
    private int grpcPort = 6334;

    /**
     * 是否使用HTTPS
     */
    @Builder.Default
    private boolean https = false;

    /**
     * API密钥（如果有）
     */
    private String apiKey;

    /**
     * 默认集合名称
     */
    @Builder.Default
    private String defaultCollectionName = "documents";

    /**
     * 构造数据库URL
     * 
     * @return HTTP URL
     */
    public String getHttpUrl() {
        String protocol = https ? "https" : "http";
        return String.format("%s://%s:%d", protocol, host, port);
    }

    /**
     * 构造gRPC地址
     * 
     * @return gRPC地址
     */
    public String getGrpcUrl() {
        return String.format("%s:%d", host, grpcPort);
    }
}