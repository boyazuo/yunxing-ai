package com.yxboot.ai.config;

import java.time.Duration;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import reactor.netty.http.client.HttpClient;

/**
 * Spring AI 基础设施配置：根据 yxboot.ai 静态配置初始化 ChatModel、EmbeddingModel 及 Qdrant 客户端。
 */
@Configuration
public class SpringAIConfig {

    @Bean
    @ConditionalOnMissingBean
    public ChatModel chatModel(AiProperties props) {
        AiProperties.ChatConfig cfg = props.getChat();
        return switch (cfg.getProvider().toLowerCase()) {
            case "zhipuai", "zhipu" -> buildZhiPuChatModel(cfg);
            default -> throw new IllegalArgumentException("不支持的 Chat 提供商: " + cfg.getProvider());
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingModel embeddingModel(AiProperties props) {
        AiProperties.EmbeddingConfig cfg = props.getEmbedding();
        return switch (cfg.getProvider().toLowerCase()) {
            case "zhipuai", "zhipu" -> buildZhiPuEmbeddingModel(cfg);
            case "dashscope", "bailian", "alibaba" -> buildDashScopeEmbeddingModel(cfg);
            case "ollama" -> buildOllamaEmbeddingModel(cfg);
            default -> throw new IllegalArgumentException("不支持的 Embedding 提供商: " + cfg.getProvider());
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public QdrantClient qdrantClient(
            @Value("${spring.ai.vectorstore.qdrant.host:localhost}") String host,
            @Value("${spring.ai.vectorstore.qdrant.port:6334}") int port,
            @Value("${spring.ai.vectorstore.qdrant.use-tls:false}") boolean useTls,
            @Value("${spring.ai.vectorstore.qdrant.api-key:}") String apiKey) {
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(host, port, useTls);
        if (StringUtils.hasText(apiKey)) {
            builder.withApiKey(apiKey);
        }
        return new QdrantClient(builder.build());
    }

    private ChatModel buildZhiPuChatModel(AiProperties.ChatConfig cfg) {
        if (!StringUtils.hasText(cfg.getApiKey())) {
            throw new IllegalArgumentException("Chat 模型 API Key 未配置，请在 yxboot.ai.chat.api-key 中设置");
        }
        ZhiPuAiApi api = ZhiPuAiApi.builder().apiKey(cfg.getApiKey()).build();
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model(cfg.getModel())
                .temperature(cfg.getTemperature())
                .topP(cfg.getTopP())
                .maxTokens(cfg.getMaxTokens())
                .build();
        return new ZhiPuAiChatModel(api, options);
    }

    private EmbeddingModel buildZhiPuEmbeddingModel(AiProperties.EmbeddingConfig cfg) {
        if (!StringUtils.hasText(cfg.getApiKey())) {
            throw new IllegalArgumentException("Embedding 模型 API Key 未配置，请在 yxboot.ai.embedding.api-key 中设置");
        }
        ZhiPuAiApi api = ZhiPuAiApi.builder().apiKey(cfg.getApiKey()).build();
        ZhiPuAiEmbeddingOptions options = ZhiPuAiEmbeddingOptions.builder()
                .model(cfg.getModel())
                .dimensions(cfg.getDimensions())
                .build();
        return new ZhiPuAiEmbeddingModel(api, MetadataMode.EMBED, options);
    }

    private EmbeddingModel buildDashScopeEmbeddingModel(AiProperties.EmbeddingConfig cfg) {
        if (!StringUtils.hasText(cfg.getApiKey())) {
            throw new IllegalArgumentException(
                    "Embedding API Key 未配置，请在 yxboot.ai.embedding.api-key 中设置百炼 DashScope API Key");
        }
        String baseUrl = StringUtils.hasText(cfg.getBaseUrl())
                ? cfg.getBaseUrl()
                : "https://dashscope.aliyuncs.com/compatible-mode";
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(cfg.getApiKey())
                .build();
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(cfg.getModel())
                .dimensions(cfg.getDimensions())
                .build();
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }

    private EmbeddingModel buildOllamaEmbeddingModel(AiProperties.EmbeddingConfig cfg) {
        String baseUrl = StringUtils.hasText(cfg.getBaseUrl()) ? cfg.getBaseUrl() : "http://localhost:11434";
        int readTimeoutSeconds = cfg.getReadTimeoutSeconds() != null && cfg.getReadTimeoutSeconds() > 0
                ? cfg.getReadTimeoutSeconds()
                : 300;
        Duration readTimeout = Duration.ofSeconds(readTimeoutSeconds);
        HttpClient httpClient = HttpClient.create().responseTimeout(readTimeout);
        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(new ReactorClientHttpRequestFactory(httpClient));
        WebClient.Builder webClientBuilder = WebClient.builder()
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient));
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(baseUrl)
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilder)
                .build();
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaEmbeddingOptions.builder()
                        .model(cfg.getModel())
                        .build())
                .build();
    }
}
