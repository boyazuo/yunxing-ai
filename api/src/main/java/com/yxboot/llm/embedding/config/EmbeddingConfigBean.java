package com.yxboot.llm.embedding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingConfig;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingModel;
import com.yxboot.llm.storage.VectorStore;
import com.yxboot.llm.storage.qdrant.QdrantConfig;
import com.yxboot.llm.storage.qdrant.QdrantVectorStore;

/**
 * 嵌入服务配置Bean
 */
@Configuration
public class EmbeddingConfigBean {

    /**
     * 智谱AI嵌入模型配置
     */
    @Bean
    @ConfigurationProperties(prefix = "yxboot.llm.embedding.zhipuai")
    public ZhipuAIEmbeddingConfig zhipuAIEmbeddingConfig() {
        return new ZhipuAIEmbeddingConfig();
    }

    /**
     * QDrant向量存储配置
     */
    @Bean
    @ConfigurationProperties(prefix = "yxboot.llm.embedding.qdrant")
    public QdrantConfig qdrantConfig() {
        return new QdrantConfig();
    }

    /**
     * 智谱AI嵌入模型
     */
    @Bean
    public EmbeddingModel embeddingModel(ZhipuAIEmbeddingConfig config) {
        return new ZhipuAIEmbeddingModel(config);
    }

    /**
     * QDrant向量存储
     */
    @Bean
    @Primary
    public VectorStore vectorStore(QdrantConfig config, EmbeddingModel embeddingModel) {
        return new QdrantVectorStore(config, embeddingModel);
    }
}