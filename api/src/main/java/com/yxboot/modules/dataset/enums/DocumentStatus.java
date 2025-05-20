package com.yxboot.modules.dataset.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 文档状态枚举
 * 
 * @author Boya
 */
@Getter
public enum DocumentStatus {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "处理完成"),
    FAILED("failed", "处理失败");

    @EnumValue
    private final String value;
    private final String desc;

    DocumentStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}