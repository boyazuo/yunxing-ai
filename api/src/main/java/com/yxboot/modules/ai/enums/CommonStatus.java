package com.yxboot.modules.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 通用状态枚举
 * 
 * @author Boya
 */
@Getter
public enum CommonStatus {

    ACTIVE("active", "激活"),
    DISABLED("disabled", "禁用");

    @EnumValue
    private final String value;
    private final String desc;

    CommonStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}