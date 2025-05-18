package com.yxboot.modules.account.enums;

import lombok.Getter;

@Getter
public enum InvitationStatus {
    PENDING("pending", "待接受"),
    ACCEPTED("accepted", "已接受");

    private final String value;
    private final String description;

    InvitationStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}