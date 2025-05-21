package com.yxboot.llm.provider.zhipu;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxboot.llm.embedding.model.AbstractEmbeddingModel;
import com.yxboot.llm.embedding.model.config.ZhipuAIEmbeddingConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 智谱AI嵌入模型实现
 */
@Slf4j
public class ZhipuAIEmbeddingModel extends AbstractEmbeddingModel {

    private final ZhipuAIEmbeddingConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     *
     * @param config 智谱AI配置
     */
    public ZhipuAIEmbeddingModel(ZhipuAIEmbeddingConfig config) {
        super(config.getBatchSize());
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
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
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 发送请求
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(config.getBaseUrl(), entity, String.class);

            // 解析响应
            JsonNode root = objectMapper.readTree(response.getBody());
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
     * 创建API请求头，包括身份验证信息
     *
     * @return HTTP头信息
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();

        try {
            // 解析API密钥
            String[] keyParts = config.getApiKey().split("\\.");
            if (keyParts.length != 2) {
                throw new IllegalArgumentException("无效的API密钥格式");
            }

            String apiKey = keyParts[0];
            String apiSecret = keyParts[1];

            // 准备签名数据
            long timestamp = Instant.now().getEpochSecond();
            String signData = apiKey + "." + timestamp;

            // 使用HMAC-SHA256计算签名
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String signature = Base64.getEncoder()
                    .encodeToString(sha256_HMAC.doFinal(signData.getBytes(StandardCharsets.UTF_8)));

            // 设置认证头
            String authorization = "Bearer " + apiKey + "." + timestamp + "." + signature;
            headers.set("Authorization", authorization);

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