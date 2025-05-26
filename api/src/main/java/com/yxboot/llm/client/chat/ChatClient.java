package com.yxboot.llm.client.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.yxboot.llm.chat.ChatModel;
import com.yxboot.llm.chat.ChatModelFactory;
import com.yxboot.llm.chat.ChatResponse;
import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.message.UserMessage;
import com.yxboot.llm.chat.prompt.ChatOptions;
import com.yxboot.llm.chat.prompt.Prompt;
import com.yxboot.modules.ai.entity.Provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 聊天客户端
 * 作为连接通用封装层(ChatModel)和业务层的桥梁
 * 提供简化的API调用接口，隐藏底层复杂性
 * 
 * 架构层次：
 * 1. 大模型原生API (如 ZhipuAIApi)
 * 2. 通用封装层 (ChatModel)
 * 3. 连接层 (ChatClient) ← 当前类
 * 4. 业务逻辑层 (上层应用的业务层)
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatClient {

    private final ChatModelFactory chatModelFactory;
    private final ChatClientConfig config;

    // 缓存已创建的ChatModel实例，避免重复创建
    private final Map<String, CachedChatModel> modelCache = new ConcurrentHashMap<>();

    /**
     * 简单文本对话
     * 
     * @param provider 提供商信息
     * @param message  用户消息
     * @return AI回复
     */
    public String chat(Provider provider, String message) {
        ChatModel chatModel = getChatModel(provider);
        return chatModel.call(message);
    }

    /**
     * 多轮对话
     * 
     * @param provider 提供商信息
     * @param messages 消息列表
     * @return AI回复
     */
    public String chat(Provider provider, List<Message> messages) {
        ChatModel chatModel = getChatModel(provider);
        return chatModel.call(messages);
    }

    /**
     * 带提示词的对话
     * 
     * @param provider 提供商信息
     * @param prompt   提示词
     * @return 聊天响应
     */
    public ChatResponse chat(Provider provider, Prompt prompt) {
        ChatModel chatModel = getChatModel(provider);
        return chatModel.call(prompt);
    }

    /**
     * 带选项的对话
     * 
     * @param provider 提供商信息
     * @param message  用户消息
     * @param options  聊天选项
     * @return 聊天响应
     */
    public ChatResponse chatWithOptions(Provider provider, String message, ChatOptions options) {
        ChatModel chatModel = getChatModel(provider);
        List<Message> messages = List.of(new UserMessage(message));
        Prompt prompt = new Prompt(messages, options);
        return chatModel.call(prompt);
    }

    /**
     * 带选项的多轮对话
     * 
     * @param provider 提供商信息
     * @param messages 消息列表
     * @param options  聊天选项
     * @return 聊天响应
     */
    public ChatResponse chatWithOptions(Provider provider, List<Message> messages, ChatOptions options) {
        ChatModel chatModel = getChatModel(provider);
        Prompt prompt = new Prompt(messages, options);
        return chatModel.call(prompt);
    }

    /**
     * 流式对话
     * 
     * @param provider 提供商信息
     * @param message  用户消息
     * @return 文本流
     */
    public Flux<String> streamChat(Provider provider, String message) {
        ChatModel chatModel = getChatModel(provider);
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt).map(ChatResponse::getContent);
    }

    /**
     * 流式多轮对话
     * 
     * @param provider 提供商信息
     * @param messages 消息列表
     * @return 文本流
     */
    public Flux<String> streamChat(Provider provider, List<Message> messages) {
        ChatModel chatModel = getChatModel(provider);
        Prompt prompt = new Prompt(messages);
        return chatModel.stream(prompt).map(ChatResponse::getContent);
    }

    /**
     * 带提示词的流式对话
     * 
     * @param provider 提供商信息
     * @param prompt   提示词
     * @return 文本流
     */
    public Flux<ChatResponse> streamChat(Provider provider, Prompt prompt) {
        ChatModel chatModel = getChatModel(provider);
        return chatModel.stream(prompt);
    }

    /**
     * 带选项的流式对话
     * 
     * @param provider 提供商信息
     * @param message  用户消息
     * @param options  聊天选项
     * @return 响应流
     */
    public Flux<ChatResponse> streamChatWithOptions(Provider provider, String message, ChatOptions options) {
        ChatModel chatModel = getChatModel(provider);
        List<Message> messages = List.of(new UserMessage(message));
        Prompt prompt = new Prompt(messages, options);
        return chatModel.stream(prompt);
    }

    /**
     * 带选项的流式多轮对话
     * 
     * @param provider 提供商信息
     * @param messages 消息列表
     * @param options  聊天选项
     * @return 响应流
     */
    public Flux<ChatResponse> streamChatWithOptions(Provider provider, List<Message> messages, ChatOptions options) {
        ChatModel chatModel = getChatModel(provider);
        Prompt prompt = new Prompt(messages, options);
        return chatModel.stream(prompt);
    }

    /**
     * 获取或创建ChatModel实例
     * 
     * @param provider 提供商信息
     * @return ChatModel实例
     */
    private ChatModel getChatModel(Provider provider) {
        String cacheKey = generateCacheKey(provider);

        CachedChatModel cachedModel = modelCache.computeIfAbsent(cacheKey, key -> {
            log.debug("为提供商 {} 创建新的ChatModel实例", provider.getProviderName());
            ChatModel chatModel = chatModelFactory.createChatModel(provider);
            return CachedChatModel.of(chatModel);
        });

        // 更新最后访问时间
        cachedModel.updateLastAccessTime();

        // 检查是否过期
        if (config.isCacheEnabled() && cachedModel.isExpired(config.getCacheExpireMinutes())) {
            log.debug("提供商 {} 的缓存已过期，重新创建", provider.getProviderName());
            modelCache.remove(cacheKey);
            ChatModel chatModel = chatModelFactory.createChatModel(provider);
            cachedModel = CachedChatModel.of(chatModel);
            modelCache.put(cacheKey, cachedModel);
        }

        return cachedModel.getChatModel();
    }

    /**
     * 生成缓存键
     * 
     * @param provider 提供商信息
     * @return 缓存键
     */
    private String generateCacheKey(Provider provider) {
        return provider.getProviderName() + ":" + provider.getApiKey().hashCode();
    }

    /**
     * 清除指定提供商的缓存
     * 
     * @param provider 提供商信息
     */
    public void clearCache(Provider provider) {
        String cacheKey = generateCacheKey(provider);
        modelCache.remove(cacheKey);
        log.debug("已清除提供商 {} 的ChatModel缓存", provider.getProviderName());
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        modelCache.clear();
        log.debug("已清除所有ChatModel缓存");
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cacheSize", modelCache.size());
        stats.put("cachedProviders", modelCache.keySet());

        // 添加缓存详细信息
        List<Map<String, Object>> cacheDetails = new ArrayList<>();
        modelCache.forEach((key, cachedModel) -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("key", key);
            detail.put("createTime", cachedModel.getCreateTime());
            detail.put("lastAccessTime", cachedModel.getLastAccessTime());
            detail.put("expired", cachedModel.isExpired(config.getCacheExpireMinutes()));
            cacheDetails.add(detail);
        });
        stats.put("cacheDetails", cacheDetails);

        return stats;
    }

    /**
     * 检查提供商是否可用
     * 
     * @param provider 提供商信息
     * @return 是否可用
     */
    public boolean isProviderAvailable(Provider provider) {
        try {
            ChatModel chatModel = getChatModel(provider);
            // 发送一个简单的测试消息
            String response = chatModel.call("Hello");
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            log.warn("提供商 {} 不可用: {}", provider.getProviderName(), e.getMessage());
            return false;
        }
    }
}