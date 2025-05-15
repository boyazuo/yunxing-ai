package com.yxboot.llm.chat.prompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天选项类
 * 用于配置大模型调用参数
 * 
 * @author Boya
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatOptions {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 温度 (0.0-2.0)
     * 较高的值会使输出更随机，较低的值会使其更加聚焦和确定
     */
    @Builder.Default
    private Float temperature = 0.7f;

    /**
     * 是否启用流式响应
     */
    @Builder.Default
    private Boolean stream = false;

    /**
     * 最多考虑的令牌数量
     * 这个参数用于控制模型在生成回复时考虑的最大令牌数
     */
    private Integer maxTokens;

    /**
     * 输出令牌的采样方法
     * 可选值: "top_p"、"top_k"、"temperature" 或组合
     */
    private String samplingMethod;

    /**
     * Top P 采样(0.0-1.0)
     * 模型只考虑具有top_p概率质量的令牌
     */
    private Float topP;

    /**
     * Top K 采样
     * 模型考虑概率最高的K个令牌
     */
    private Integer topK;

    /**
     * 停止序列
     * 模型生成文本时遇到这些序列会停止
     */
    private List<String> stop;

    /**
     * 重复惩罚(0.0-2.0)
     * 用于减少输出中的重复内容
     */
    private Float repetitionPenalty;

    /**
     * 频率惩罚(0.0-2.0)
     * 用于减少常见令牌的使用
     */
    private Float frequencyPenalty;

    /**
     * 存在惩罚(0.0-2.0)
     * 用于增加输出中的新颖内容
     */
    private Float presencePenalty;

    /**
     * 系统特定参数
     * 不同模型提供商可能需要的特定参数
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();

    /**
     * 设置参数
     * 
     * @param key   参数名
     * @param value 参数值
     * @return 聊天选项
     */
    public ChatOptions withParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    /**
     * 获取所有参数的映射
     * 
     * @return 参数映射
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        if (model != null)
            map.put("model", model);
        map.put("temperature", temperature);
        map.put("stream", stream);

        if (maxTokens != null)
            map.put("max_tokens", maxTokens);
        if (topP != null)
            map.put("top_p", topP);
        if (topK != null)
            map.put("top_k", topK);
        if (stop != null && !stop.isEmpty())
            map.put("stop", stop);
        if (repetitionPenalty != null)
            map.put("repetition_penalty", repetitionPenalty);
        if (frequencyPenalty != null)
            map.put("frequency_penalty", frequencyPenalty);
        if (presencePenalty != null)
            map.put("presence_penalty", presencePenalty);

        // 添加其他自定义参数
        map.putAll(parameters);

        return map;
    }
}