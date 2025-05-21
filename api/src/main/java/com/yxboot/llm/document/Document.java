package com.yxboot.llm.document;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表示一个处理后的文档
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    /**
     * 文档内容
     */
    private String content;

    /**
     * 文档元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 创建一个新的文档实例
     * 
     * @param content 文档内容
     * @return 文档实例
     */
    public static Document of(String content) {
        return Document.builder().content(content).build();
    }

    /**
     * 创建一个新的文档实例
     * 
     * @param content  文档内容
     * @param metadata 文档元数据
     * @return 文档实例
     */
    public static Document of(String content, Map<String, Object> metadata) {
        return Document.builder().content(content).metadata(metadata).build();
    }

    /**
     * 添加元数据
     * 
     * @param key   键
     * @param value 值
     * @return 当前文档实例
     */
    public Document addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
}