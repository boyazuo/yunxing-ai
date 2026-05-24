package com.yxboot.ai.rag;

import lombok.Builder;
import lombok.Data;

/**
 * 知识库 RAG 提示词构建结果。
 */
@Data
@Builder
public class RagPromptResult {

    /** 是否启用了知识库严格模式 */
    private boolean ragMode;

    /** 是否检索到可用参考内容 */
    private boolean hasRetrievedContext;

    /** 合并后的系统提示词 */
    private String systemPrompt;

    /** 发送给模型的用户消息 */
    private String userPrompt;

    /**
     * 非空时表示无需调用 LLM，直接返回该固定回复。
     * 通常用于严格模式下未检索到相关内容的情况。
     */
    private String directResponse;
}
