package com.yxboot.llm.embedding.config;

/**
 * 嵌入服务配置接口
 */
public interface EmbeddingConfig {

    String getApiKey();

    String getModelName();

    String getBaseUrl();

    int getEmbeddingDimension();

    int getBatchSize();
}