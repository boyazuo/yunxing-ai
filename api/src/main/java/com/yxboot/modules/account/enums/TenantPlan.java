package com.yxboot.modules.account.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum TenantPlan {
    FREE("free", "免费"),
    BASIC("basic", "基础版"),
    PRO("pro", "专业版");

    @EnumValue
    private final String value;
    private final String desc;

    TenantPlan(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}