package com.yxboot.llm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingConfig;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingModel;
import com.yxboot.llm.vector.VectorStore;
import com.yxboot.llm.vector.qdrant.QdrantConfig;
import com.yxboot.llm.vector.qdrant.QdrantVectorStore;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class LlmAutoConfigureation {

    private final EmbeddingProperties embeddings;
    private final VectorProperties vectors;

    /**
     * 智谱AI嵌入模型配置
     */
    @Bean
    public ZhipuAIEmbeddingConfig zhipuAIEmbeddingConfig() {
        EmbeddingProperties.ZhipuAIEmbeddingProperties zhipuaiProperties = embeddings.getZhipuai();
        return ZhipuAIEmbeddingConfig.builder()
                .apiKey(zhipuaiProperties.getApiKey())
                .modelName(zhipuaiProperties.getModelName())
                .baseUrl(zhipuaiProperties.getBaseUrl())
                .embeddingDimension(zhipuaiProperties.getEmbeddingDimension())
                .batchSize(zhipuaiProperties.getBatchSize())
                .build();
    }

    /**
     * 默认嵌入模型实例
     */
    @Bean
    @Primary
    public EmbeddingModel embeddingModel(ZhipuAIEmbeddingConfig config) {
        return new ZhipuAIEmbeddingModel(config);
    }

    /**
     * QDrant向量存储配置
     */
    @Bean
    public QdrantConfig qdrantConfig() {
        VectorProperties.QdrantProperties qdrantProperties = vectors.getQdrant();
        return QdrantConfig.builder()
                .host(qdrantProperties.getHost())
                .port(qdrantProperties.getPort())
                .grpcPort(qdrantProperties.getGrpcPort())
                .https(qdrantProperties.isHttps())
                .apiKey(qdrantProperties.getApiKey())
                .defaultCollectionName(qdrantProperties.getDefaultCollectionName())
                .build();
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
