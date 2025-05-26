package com.yxboot.llm.provider.zhipu.kernel;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZhipuAIMessage {
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
