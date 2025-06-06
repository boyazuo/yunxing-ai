package com.yxboot.modules.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 消息状态枚举
 * 
 * @author Boya
 */
@Getter
public enum MessageStatus {

    PENDING("pending", "处理中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败");

    @EnumValue
    private final String value;
    private final String desc;

    MessageStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}