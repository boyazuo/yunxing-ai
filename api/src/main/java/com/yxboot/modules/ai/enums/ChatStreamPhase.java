package com.yxboot.modules.ai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 流式聊天处理阶段，通过 SSE status 事件推送给前端。
 */
@Getter
@RequiredArgsConstructor
public enum ChatStreamPhase {

    UNDERSTANDING("understanding"),
    RETRIEVING("retrieving"),
    GENERATING("generating");

    private final String value;
}
