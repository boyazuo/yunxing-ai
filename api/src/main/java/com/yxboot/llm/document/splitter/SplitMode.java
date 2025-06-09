package com.yxboot.llm.document.splitter;

/**
 * 文档分段方式枚举
 * 
 * @author Boya
 */
public enum SplitMode {

    /**
     * 按字符分割 - 按照指定的字符长度进行分割
     */
    CHARACTER_SPLITTER("按字符分割"),

    /**
     * 按章节分割 - 基于文档结构智能识别章节进行分割
     */
    CHAPTER_SPLITTER("按章节分割");

    private final String description;

    SplitMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
