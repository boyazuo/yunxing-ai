package com.yxboot.modules.dataset.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 文档分段方式枚举
 * 
 * @author Boya
 */
@Getter
public enum SegmentMethod {
    FIXED_LENGTH("fixed_length", "固定长度"),
    PARAGRAPH("paragraph", "段落"),
    SENTENCE("sentence", "句子"),
    HYBRID("hybrid", "混合方式");

    @EnumValue
    private final String value;
    private final String desc;

    SegmentMethod(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}