package com.yxboot.llm.document;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表示文档分割后的块
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSegment {
    /**
     * 块ID
     */
    private String id;

    /**
     * 块标题
     */
    private String title;

    /**
     * 块内容
     */
    private String content;

    /**
     * 块元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 创建一个新的文档块实例
     * 
     * @param content 块内容
     * @return 文档块实例
     */
    public static DocumentSegment of(String title, String content) {
        return DocumentSegment.builder().title(title).content(content).build();
    }

    /**
     * 创建一个新的文档块实例
     * 
     * @param id      块ID
     * @param content 块内容
     * @return 文档块实例
     */
    public static DocumentSegment of(String id, String title, String content) {
        return DocumentSegment.builder().id(id).title(title).content(content).build();
    }

    /**
     * 创建一个新的文档块实例
     * 
     * @param content  块内容
     * @param metadata 块元数据
     * @return 文档块实例
     */
    public static DocumentSegment of(String title, String content, Map<String, Object> metadata) {
        return DocumentSegment.builder().title(title).content(content).metadata(metadata).build();
    }

    /**
     * 添加元数据
     * 
     * @param key   键
     * @param value 值
     * @return 当前文档块实例
     */
    public DocumentSegment addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
}