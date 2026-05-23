package com.yxboot.llm.client.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * 嵌入客户端配置
 * 
 * @author Boya
 */
@Data
@Component
@ConfigurationProperties(prefix = "yxboot.llm.embedding.client")
public class EmbeddingClientConfig {

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 缓存最大大小
     */
    private int maxCacheSize = 100;

    /**
     * 缓存过期时间（分钟）
     */
    private long cacheExpireMinutes = 30;

    /**
     * 默认超时时间（秒）
     */
    private int defaultTimeoutSeconds = 600;

    /**
     * 是否启用健康检查
     */
    private boolean healthCheckEnabled = false;

    /**
     * 健康检查间隔（分钟）
     */
    private long healthCheckIntervalMinutes = 5;

    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * 重试配置内部类
     */
    @Data
    public static class RetryConfig {
        /**
         * 是否启用重试
         */
        private boolean enabled = true;

        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;

        /**
         * 重试间隔（毫秒）
         */
        private long delayMillis = 2000;

        /**
         * 重试间隔倍数
         */
        private double multiplier = 2.0;
    }
}
