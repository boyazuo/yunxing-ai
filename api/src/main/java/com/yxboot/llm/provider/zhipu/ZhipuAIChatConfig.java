package com.yxboot.llm.provider.zhipu;

import lombok.Builder;
import lombok.Data;

/**
 * 知启API配置类
 * 统一管理知启API调用参数
 * 
 * @author Boya
 */
@Data
@Builder
public class ZhipuAIChatConfig {

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 模型名称
     */
    @Builder.Default
    private String model = "glm-4";

    /**
     * API基础URL
     */
    @Builder.Default
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    /**
     * 温度参数(0-1)
     */
    @Builder.Default
    private Float temperature = 0.7f;

    /**
     * 最大生成Token数
     */
    @Builder.Default
    private Integer maxTokens = 2048;

    /**
     * Top-P参数(0-1)
     */
    @Builder.Default
    private Float topP = 0.8f;

    /**
     * 使用默认API密钥创建配置
     * 
     * @param apiKey API密钥
     * @return 知启配置
     */
    public static ZhipuAIChatConfig of(String apiKey) {
        return ZhipuAIChatConfig.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * 创建带自定义模型的配置
     * 
     * @param apiKey API密钥
     * @param model  模型名称
     * @return 知启配置
     */
    public static ZhipuAIChatConfig of(String apiKey, String model) {
        return ZhipuAIChatConfig.builder()
                .apiKey(apiKey)
                .model(model)
                .build();
    }

    /**
     * 设置API密钥
     * 
     * @param apiKey API密钥
     * @return 当前配置对象
     */
    public ZhipuAIChatConfig withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * 设置温度
     * 
     * @param temperature 温度参数
     * @return 当前配置对象
     */
    public ZhipuAIChatConfig withTemperature(float temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * 设置最大Token数
     * 
     * @param maxTokens 最大Token数
     * @return 当前配置对象
     */
    public ZhipuAIChatConfig withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * 设置Top-P参数
     * 
     * @param topP Top-P参数
     * @return 当前配置对象
     */
    public ZhipuAIChatConfig withTopP(float topP) {
        this.topP = topP;
        return this;
    }

    /**
     * 设置API基础URL
     * 
     * @param baseUrl API基础URL
     * @return 当前配置对象
     */
    public ZhipuAIChatConfig withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * 创建默认API配置
     * 
     * @return 带默认值的配置
     */
    public static ZhipuAIChatConfig defaultConfig() {
        return ZhipuAIChatConfig.builder().build();
    }
}