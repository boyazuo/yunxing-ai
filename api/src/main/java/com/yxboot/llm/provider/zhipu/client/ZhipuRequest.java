package com.yxboot.llm.provider.zhipu.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知启API请求模型
 * 
 * @author Boya
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZhipuRequest {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    @Builder.Default
    private List<ZhipuMessage> messages = new ArrayList<>();

    /**
     * 温度参数(0-1)
     */
    private Float temperature;

    /**
     * 是否启用流式响应
     */
    private Boolean stream;

    /**
     * 系统指令
     */
    private String system;

    /**
     * 最大生成token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * Top P采样率(0-1)
     */
    @JsonProperty("top_p")
    private Float topP;

    /**
     * Top K采样率
     */
    @JsonProperty("top_k")
    private Integer topK;

    /**
     * 停止序列
     */
    private List<String> stop;

    /**
     * 工具调用定义
     */
    private List<Map<String, Object>> tools;

    /**
     * 知启消息模型
     */
    @Data
    @Builder
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

        /**
         * 工具结果
         */
        @JsonProperty("tool_call_id")
        private String toolCallId;
    }
}