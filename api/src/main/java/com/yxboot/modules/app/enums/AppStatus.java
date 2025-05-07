package com.yxboot.modules.app.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 应用状态枚举
 * 
 * @author Boya
 */
@Getter
public enum AppStatus {

    DRAFT("draft", "草稿"),
    PUBLISHED("published", "已发布"),
    DISABLED("disabled", "已禁用"),
    DELETED("deleted", "已删除");

    @EnumValue
    private final String value;
    private final String desc;

    AppStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}