package com.yxboot.llm.client.vector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * VectorRetrieverClient配置类 用于管理向量检索客户端的配置参数
 * 
 * @author Boya
 */
@Data
@Component
@ConfigurationProperties(prefix = "yxboot.llm.retriever.client")
public class VectorRetrieverConfig {

    /**
     * 默认返回结果数量限制
     */
    private int defaultLimit = 10;

    /**
     * 默认最小相似度阈值
     */
    private float defaultMinScore = 0.0f;

    /**
     * 集合名称前缀
     */
    private String collectionPrefix = "dataset_";

    /**
     * 是否启用Provider缓存
     */
    private boolean providerCacheEnabled = true;

    /**
     * Provider缓存最大大小
     */
    private int maxProviderCacheSize = 100;

    /**
     * Provider缓存过期时间（分钟）
     */
    private long providerCacheExpireMinutes = 60;

    /**
     * 是否启用健康检查
     */
    private boolean healthCheckEnabled = true;

    /**
     * 健康检查间隔（分钟）
     */
    private long healthCheckIntervalMinutes = 10;

    /**
     * 混合检索配置
     */
    private HybridConfig hybrid = new HybridConfig();

    /**
     * 混合检索配置内部类
     */
    @Data
    public static class HybridConfig {
        /**
         * 是否启用混合检索
         */
        private boolean enabled = false;

        /**
         * 语义检索权重
         */
        private float semanticWeight = 0.7f;

        /**
         * 关键词检索权重
         */
        private float keywordWeight = 0.3f;

        /**
         * 重排序算法
         */
        private String rerankAlgorithm = "rrf"; // reciprocal rank fusion
    }
}
