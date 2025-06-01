package com.yxboot.llm.provider.zhipu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxboot.llm.embedding.config.EmbeddingConfig;
import com.yxboot.llm.embedding.model.AbstractEmbeddingModel;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.embedding.model.EmbeddingRequest;
import com.yxboot.llm.embedding.model.EmbeddingResponse;
import com.yxboot.llm.embedding.model.EmbeddingResponse.EmbeddingResult;
import com.yxboot.llm.embedding.model.EmbeddingResponse.TokenUsage;
import com.yxboot.util.HttpClient;

import lombok.extern.slf4j.Slf4j;

/**
 * 智谱AI嵌入模型实现
 */
@Slf4j
public class ZhipuAIEmbeddingModel extends AbstractEmbeddingModel {

    private ZhipuAIEmbeddingConfig config;
    private ObjectMapper objectMapper;

    public ZhipuAIEmbeddingModel() {
        this.config = new ZhipuAIEmbeddingConfig();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 构造函数
     *
     * @param config 智谱AI配置
     */
    public ZhipuAIEmbeddingModel(ZhipuAIEmbeddingConfig config) {
        super(config.getBatchSize());
        this.config = config;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void configure(EmbeddingConfig config) {
        this.config = (ZhipuAIEmbeddingConfig) config;
    }

    /**
     * 设置API密钥
     *
     * @param apiKey API密钥
     * @return 当前模型实例
     */
    @Override
    public EmbeddingModel withApiKey(String apiKey) {
        if (this.config == null) {
            this.config = new ZhipuAIEmbeddingConfig();
        }
        this.config.setApiKey(apiKey);
        return this;
    }

    /**
     * 将单个文本编码为向量
     *
     * @param text 输入文本
     * @return 向量表示
     */
    @Override
    public float[] embed(String text) {
        List<String> texts = new ArrayList<>();
        texts.add(text);
        List<float[]> embeddings = embedBatch(texts);
        return embeddings.isEmpty() ? new float[getEmbeddingDimension()] : embeddings.get(0);
    }

    /**
     * 批量处理文本嵌入
     *
     * @param batch 文本批次
     * @return 向量列表
     */
    @Override
    protected List<float[]> embedBatch(List<String> batch) {
        if (batch == null || batch.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 准备请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName());
            requestBody.put("input", batch);

            // 准备请求头
            Map<String, String> headers = createHeaders();

            // 序列化请求体
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 发送请求
            String response = HttpClient.postJson(config.getBaseUrl(), jsonBody, headers);

            // 解析响应
            JsonNode root = objectMapper.readTree(response);
            JsonNode dataNode = root.get("data");

            List<float[]> embeddings = new ArrayList<>();
            for (JsonNode item : dataNode) {
                JsonNode embeddingNode = item.get("embedding");

                float[] embedding = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    embedding[i] = (float) embeddingNode.get(i).asDouble();
                }

                embeddings.add(embedding);
            }

            return embeddings;
        } catch (Exception e) {
            log.error("嵌入处理失败", e);
            throw new RuntimeException("嵌入处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理嵌入请求并返回嵌入响应
     * 直接调用智谱AI的嵌入API，优化性能
     *
     * @param request 嵌入请求对象
     * @return 嵌入响应对象
     */
    @Override
    public EmbeddingResponse embedRequest(EmbeddingRequest request) {
        if (request == null || request.getInput() == null || request.getInput().isEmpty()) {
            return EmbeddingResponse.builder()
                    .modelName(getModelName())
                    .build();
        }

        try {
            // 准备请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName());
            requestBody.put("input", request.getInput());

            // 如果请求中有指定维度，添加到请求体
            if (request.getDimensions() != null) {
                requestBody.put("dimensions", request.getDimensions());
            }

            // 添加额外参数
            if (request.getOptions() != null) {
                requestBody.putAll(request.getOptions());
            }

            // 准备请求头
            Map<String, String> headers = createHeaders();

            // 序列化请求体
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 发送请求
            String responseBody = HttpClient.postJson(config.getBaseUrl(), jsonBody, headers);

            // 解析响应
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataNode = root.get("data");
            JsonNode usageNode = root.get("usage");

            // 提取token使用情况
            TokenUsage tokenUsage = null;
            if (usageNode != null) {
                int inputTokens = usageNode.has("prompt_tokens") ? usageNode.get("prompt_tokens").asInt()
                        : calculateTokens(request.getInput());

                tokenUsage = TokenUsage.of(inputTokens);
            } else {
                tokenUsage = TokenUsage.of(calculateTokens(request.getInput()));
            }

            // 构建结果列表
            List<EmbeddingResult> results = new ArrayList<>();
            for (int i = 0; i < dataNode.size(); i++) {
                JsonNode item = dataNode.get(i);
                JsonNode embeddingNode = item.get("embedding");
                int index = item.has("index") ? item.get("index").asInt() : i;
                String object = item.has("object") ? item.get("object").asText() : "embedding";

                float[] embedding = new float[embeddingNode.size()];
                for (int j = 0; j < embeddingNode.size(); j++) {
                    embedding[j] = (float) embeddingNode.get(j).asDouble();
                }

                results.add(EmbeddingResult.builder()
                        .index(index)
                        .object(object)
                        .embedding(embedding)
                        .build());
            }

            // 构建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", config.getModelName());
            if (root.has("id")) {
                metadata.put("id", root.get("id").asText());
            }
            if (root.has("created")) {
                metadata.put("created", root.get("created").asLong());
            }

            // 构建并返回响应
            return EmbeddingResponse.builder()
                    .modelName(config.getModelName())
                    .data(results)
                    .tokenUsage(tokenUsage)
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            log.error("嵌入处理失败", e);
            throw new RuntimeException("嵌入处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建API请求头，包括身份验证信息
     *
     * @return HTTP头信息
     */
    private Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        try {
            String apiKey = config.getApiKey();
            // 设置认证头
            String authorization = "Bearer " + apiKey;
            headers.put("Authorization", authorization);

        } catch (Exception e) {
            log.error("创建认证头失败", e);
            throw new RuntimeException("创建认证头失败: " + e.getMessage(), e);
        }

        return headers;
    }

    @Override
    public String getModelName() {
        return config.getModelName();
    }

    @Override
    public int getEmbeddingDimension() {
        return config.getEmbeddingDimension();
    }
}