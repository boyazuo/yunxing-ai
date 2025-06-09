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
    PARAGRAPH("paragraph", "段落"),
    CHAPTER("chapter", "章节");

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
