package com.yxboot.modules.account.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 租户成员角色枚举
 * 
 * @author Boya
 */
@Getter
public enum TenantUserRole {

    OWNER("owner", "所有者"),
    ADMIN("admin", "管理员"),
    NORMAL("normal", "普通成员");

    @EnumValue
    private final String value;
    private final String desc;

    TenantUserRole(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}