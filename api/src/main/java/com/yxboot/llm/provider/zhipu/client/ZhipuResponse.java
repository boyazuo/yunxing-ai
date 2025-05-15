package com.yxboot.llm.provider.zhipu.client;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知启API响应模型
 * 
 * @author Boya
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZhipuResponse {

    /**
     * 响应ID
     */
    private String id;

    /**
     * 创建时间
     */
    private Long created;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 响应类型
     */
    private String object;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 选择结果
     */
    private List<Choice> choices;

    /**
     * 令牌使用情况
     */
    private Usage usage;

    /**
     * 选择结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        /**
         * 索引
         */
        private Integer index;

        /**
         * 消息
         */
        private ZhipuMessage message;

        /**
         * 结束原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;

        /**
         * 流式响应的delta，只在流式响应中使用
         */
        private Delta delta;
    }

    /**
     * 令牌使用情况
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 提示令牌数
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        /**
         * 完成令牌数
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        /**
         * 总令牌数
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }

    /**
     * 知启消息模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZhipuMessage {
        /**
         * 角色
         */
        private String role;

        /**
         * 内容
         */
        private String content;

        /**
         * 工具调用
         */
        @JsonProperty("tool_calls")
        private List<Map<String, Object>> toolCalls;
    }

    /**
     * 流式响应的增量内容
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Delta {
        /**
         * 角色
         */
        private String role;

        /**
         * 内容
         */
        private String content;

        /**
         * 工具调用
         */
        @JsonProperty("tool_calls")
        private List<Map<String, Object>> toolCalls;
    }
}