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
public class DocumentChunk {
    /**
     * 块ID
     */
    private String id;

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
    public static DocumentChunk of(String content) {
        return DocumentChunk.builder().content(content).build();
    }

    /**
     * 创建一个新的文档块实例
     * 
     * @param id      块ID
     * @param content 块内容
     * @return 文档块实例
     */
    public static DocumentChunk of(String id, String content) {
        return DocumentChunk.builder().id(id).content(content).build();
    }

    /**
     * 创建一个新的文档块实例
     * 
     * @param content  块内容
     * @param metadata 块元数据
     * @return 文档块实例
     */
    public static DocumentChunk of(String content, Map<String, Object> metadata) {
        return DocumentChunk.builder().content(content).metadata(metadata).build();
    }

    /**
     * 添加元数据
     * 
     * @param key   键
     * @param value 值
     * @return 当前文档块实例
     */
    public DocumentChunk addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
}