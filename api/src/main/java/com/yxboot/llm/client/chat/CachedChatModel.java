package com.yxboot.llm.client.chat;

import java.time.LocalDateTime;

import com.yxboot.llm.chat.ChatModel;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 缓存的聊天模型包装类
 * 用于在ChatClient中管理ChatModel实例的缓存
 * 
 * @author Boya
 */
@Data
@AllArgsConstructor
public class CachedChatModel {

    /**
     * 聊天模型实例
     */
    private ChatModel chatModel;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 创建缓存的聊天模型
     * 
     * @param chatModel 聊天模型实例
     * @return 缓存的聊天模型
     */
    public static CachedChatModel of(ChatModel chatModel) {
        LocalDateTime now = LocalDateTime.now();
        return new CachedChatModel(chatModel, now, now);
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