package com.yxboot.modules.app.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 应用类型枚举
 * 
 * @author Boya
 */
@Getter
public enum AppType {

    CHAT("chat", "对话应用"),
    AGENT("agent", "智能体应用"),
    WORKFLOW("workflow", "工作流应用");

    @EnumValue
    private final String value;
    private final String desc;

    AppType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}