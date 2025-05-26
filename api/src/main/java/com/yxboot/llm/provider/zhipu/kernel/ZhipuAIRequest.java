package com.yxboot.llm.provider.zhipu.kernel;

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
public class ZhipuAIRequest {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    @Builder.Default
    private List<ZhipuAIMessage> messages = new ArrayList<>();

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

}