package com.yxboot.modules.account.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 租户状态枚举
 * 
 * @author Boya
 */
@Getter
public enum TenantStatus {

    ACTIVE("active", "活跃"),
    CLOSED("closed", "已关闭");

    @EnumValue
    private final String value;
    private final String desc;

    TenantStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}