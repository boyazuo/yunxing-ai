package com.yxboot.modules.account.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 用户状态枚举
 * 
 * @author Boya
 */
@Getter
public enum UserStatus {

    PENDING("pending", "待处理"),
    UNINITIALIZED("uninitialized", "未初始化"),
    ACTIVE("active", "活跃"),
    BANNED("banned", "已禁止"),
    CLOSED("closed", "已关闭");

    @EnumValue
    private final String value;
    private final String desc;

    UserStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}