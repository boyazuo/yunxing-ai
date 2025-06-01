package com.yxboot.llm.client.embedding;

import java.time.LocalDateTime;

import com.yxboot.llm.embedding.model.EmbeddingModel;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 缓存的嵌入模型包装类
 * 用于在EmbeddingClient中管理EmbeddingModel实例的缓存
 * 
 * @author Boya
 */
@Data
@AllArgsConstructor
public class CachedEmbeddingModel {

    /**
     * 嵌入模型实例
     */
    private EmbeddingModel embeddingModel;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 创建缓存的嵌入模型
     * 
     * @param embeddingModel 嵌入模型实例
     * @return 缓存的嵌入模型
     */
    public static CachedEmbeddingModel of(EmbeddingModel embeddingModel) {
        LocalDateTime now = LocalDateTime.now();
        return new CachedEmbeddingModel(embeddingModel, now, now);
    }

    /**
     * 更新最后访问时间
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 检查是否已过期
     * 
     * @param expireMinutes 过期时间（分钟）
     * @return 是否已过期
     */
    public boolean isExpired(long expireMinutes) {
        return lastAccessTime.plusMinutes(expireMinutes).isBefore(LocalDateTime.now());
    }
} 