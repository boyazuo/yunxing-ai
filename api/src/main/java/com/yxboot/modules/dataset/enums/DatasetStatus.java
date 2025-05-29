package com.yxboot.modules.dataset.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 知识库状态枚举
 * 
 * @author Boya
 */
@Getter
public enum DatasetStatus {
    ACTIVE("active", "正常"),
    DISABLED("disabled", "已禁用"),
    DELETED("deleted", "已删除");

    @EnumValue
    private final String value;
    private final String desc;

    DatasetStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}