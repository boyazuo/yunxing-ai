package com.yxboot.llm.provider.zhipu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.yxboot.llm.chat.ModelProvider;
import com.yxboot.llm.embedding.config.EmbeddingConfig;
import com.yxboot.llm.embedding.model.AbstractEmbeddingModel;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.embedding.model.EmbeddingRequest;
import com.yxboot.llm.embedding.model.EmbeddingResponse;
import com.yxboot.llm.embedding.model.EmbeddingResponse.EmbeddingResult;
import com.yxboot.llm.embedding.model.EmbeddingResponse.TokenUsage;
import com.yxboot.util.HttpClient;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 智谱AI嵌入模型实现
 * 
 * 重构说明： - 设计为不可变对象，所有配置在构建时设置 - 使用 Builder 模式创建实例 - 移除可变状态的 setter 方法和 configure 方法 - 使用 Hutool
 * JSONUtil 替换 ObjectMapper
 * 
 * @author Boya
 */
@Slf4j
public final class ZhipuAIEmbeddingModel extends AbstractEmbeddingModel {

    /**
     * 智谱AI配置（不可变）
     */
    private final ZhipuAIEmbeddingConfig config;

    /**
     * 私有构造函数，只能通过 Builder 创建
     * 
     * @param builder 构建器
     */
    private ZhipuAIEmbeddingModel(Builder builder) {
        super(builder.config != null ? builder.config.getBatchSize() : 32);
        this.config = builder.config;

        // 验证必要参数
        if (config == null) {
            throw new IllegalArgumentException("ZhipuAIEmbeddingConfig 不能为空");
        }
        if (StrUtil.isBlank(config.getApiKey())) {
            throw new IllegalArgumentException("API密钥不能为空");
        }
    }

    /**
     * 创建 Builder 实例
     * 
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类
     */
    public static class Builder {
        private ZhipuAIEmbeddingConfig config;

        private Builder() {}

        /**
         * 设置配置
         * 
         * @param config 配置对象
         * @return Builder 实例
         */
        public Builder config(ZhipuAIEmbeddingConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置 API 密钥
         * 
         * @param apiKey API 密钥
         * @return Builder 实例
         */
        public Builder apiKey(String apiKey) {
            if (this.config != null) {
                this.config = this.config.withApiKey(apiKey);
            } else {
                this.config = ZhipuAIEmbeddingConfig.builder().apiKey(apiKey).build();
            }
            return this;
        }

        /**
         * 设置模型名称
         * 
         * @param modelName 模型名称
         * @return Builder 实例
         */
        public Builder modelName(String modelName) {
            if (this.config != null) {
                this.config = this.config.withModelName(modelName);
            } else {
                this.config = ZhipuAIEmbeddingConfig.builder().modelName(modelName).build();
            }
            return this;
        }

        /**
         * 设置向量维度
         * 
         * @param embeddingDimension 向量维度
         * @return Builder 实例
         */
        public Builder embeddingDimension(int embeddingDimension) {
            if (this.config != null) {
                this.config = this.config.withEmbeddingDimension(embeddingDimension);
            } else {
                this.config = ZhipuAIEmbeddingConfig.builder().embeddingDimension(embeddingDimension).build();
            }
            return this;
        }

        /**
         * 设置批处理大小
         * 
         * @param batchSize 批处理大小
         * @return Builder 实例
         */
        public Builder batchSize(int batchSize) {
            if (this.config != null) {
                this.config = this.config.withBatchSize(batchSize);
            } else {
                this.config = ZhipuAIEmbeddingConfig.builder().batchSize(batchSize).build();
            }
            return this;
        }

        /**
         * 构建 ZhipuAIEmbeddingModel 实例
         * 
         * @return ZhipuAIEmbeddingModel 实例
         */
        public ZhipuAIEmbeddingModel build() {
            return new ZhipuAIEmbeddingModel(this);
        }
    }

    /**
     * 获取API密钥
     * 
     * @return API密钥
     */
    public String getApiKey() {
        return config.getApiKey();
    }

    /**
     * 获取提供商信息
     */
    @Override
    public ModelProvider getProvider() {
        return ModelProvider.ZHIPU;
    }

    /**
     * @deprecated 不再支持动态配置，请使用 Builder 模式创建新实例
     */
    @Deprecated
    @Override
    public void configure(EmbeddingConfig config) {
        throw new UnsupportedOperationException("ZhipuAIEmbeddingModel 是不可变对象，请使用 Builder 模式创建新实例");
    }

    /**
     * @deprecated 不再支持动态设置API密钥，请使用 Builder 模式创建新实例
     */
    @Deprecated
    @Override
    public EmbeddingModel withApiKey(String apiKey) {
        throw new UnsupportedOperationException("ZhipuAIEmbeddingModel 是不可变对象，请使用 Builder 模式创建新实例");
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

            // 添加维度参数，确保 ZhipuAI 返回指定维度的向量
            requestBody.put("dimensions", config.getEmbeddingDimension());

            // 准备请求头
            Map<String, String> headers = createHeaders();

            // 序列化请求体
            String jsonBody = JSONUtil.toJsonStr(requestBody);

            // 发送请求
            String response = HttpClient.postJson(config.getBaseUrl(), jsonBody, headers);

            // 解析响应
            JSONObject root = JSONUtil.parseObj(response);
            JSONArray dataArray = root.getJSONArray("data");

            List<float[]> embeddings = new ArrayList<>();
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject item = dataArray.getJSONObject(i);
                JSONArray embeddingArray = item.getJSONArray("embedding");

                float[] embedding = new float[embeddingArray.size()];
                for (int j = 0; j < embeddingArray.size(); j++) {
                    embedding[j] = embeddingArray.getFloat(j);
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
     * 处理嵌入请求并返回嵌入响应 直接调用智谱AI的嵌入API，优化性能
     *
     * @param request 嵌入请求对象
     * @return 嵌入响应对象
     */
    @Override
    public EmbeddingResponse embedRequest(EmbeddingRequest request) {
        if (request == null || request.getInput() == null || request.getInput().isEmpty()) {
            return EmbeddingResponse.builder().modelName(getModelName()).build();
        }

        try {
            // 准备请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName());
            requestBody.put("input", request.getInput());

            // 如果请求中有指定维度，添加到请求体
            if (request.getDimensions() != null) {
                requestBody.put("dimensions", request.getDimensions());
            } else {
                requestBody.put("dimensions", config.getEmbeddingDimension());
            }

            // 添加额外参数
            if (request.getOptions() != null) {
                requestBody.putAll(request.getOptions());
            }

            // 准备请求头
            Map<String, String> headers = createHeaders();

            // 序列化请求体
            String jsonBody = JSONUtil.toJsonStr(requestBody);

            // 发送请求
            String responseBody = HttpClient.postJson(config.getBaseUrl(), jsonBody, headers);

            // 解析响应
            JSONObject root = JSONUtil.parseObj(responseBody);
            JSONArray dataArray = root.getJSONArray("data");
            JSONObject usageObject = root.getJSONObject("usage");

            // 提取token使用情况
            TokenUsage tokenUsage = null;
            if (usageObject != null) {
                int inputTokens = usageObject.getInt("prompt_tokens", calculateTokens(request.getInput()));
                tokenUsage = TokenUsage.of(inputTokens);
            } else {
                tokenUsage = TokenUsage.of(calculateTokens(request.getInput()));
            }

            // 构建结果列表
            List<EmbeddingResult> results = new ArrayList<>();
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject item = dataArray.getJSONObject(i);
                JSONArray embeddingArray = item.getJSONArray("embedding");
                int index = item.getInt("index", i);
                String object = item.getStr("object", "embedding");

                float[] embedding = new float[embeddingArray.size()];
                for (int j = 0; j < embeddingArray.size(); j++) {
                    embedding[j] = embeddingArray.getFloat(j);
                }

                results.add(EmbeddingResult.builder().index(index).object(object).embedding(embedding).build());
            }

            // 构建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", config.getModelName());
            if (root.containsKey("id")) {
                metadata.put("id", root.getStr("id"));
            }
            if (root.containsKey("created")) {
                metadata.put("created", root.getLong("created"));
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
