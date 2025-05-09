package com.yxboot.modules.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 模型类型枚举
 * 
 * @author Boya
 */
@Getter
public enum ModelType {

    CHAT("chat", "对话模型"),
    REASON("reason", "推理模型");

    @EnumValue
    private final String value;
    private final String desc;

    ModelType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}