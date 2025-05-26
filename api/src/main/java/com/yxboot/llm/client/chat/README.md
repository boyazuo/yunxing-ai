# ChatClient 使用指南

## 概述

ChatClient 是云行 AI 系统中连接通用封装层（ChatModel）和业务层的桥梁，提供简化的 API 调用接口，隐藏底层复杂性。

## 架构层次

```
1. 大模型原生 API (如 ZhipuAIApi)
   ↓
2. 通用封装层 (ChatModel)
   ↓
3. 连接层 (ChatClient) ← 当前组件
   ↓
4. 业务逻辑层 (上层应用的业务层)
```

## 核心组件

### 1. ChatClient
基础聊天客户端，提供核心的对话功能：
- 简单文本对话
- 多轮对话
- 带选项的对话
- 流式对话
- 缓存管理
- 健康检查

### 2. EnhancedChatClient
增强版聊天客户端，在基础功能上添加：
- 重试机制
- 异步处理
- 批量处理
- 定时健康检查
- 自动缓存清理

### 3. ChatClientConfig
配置管理类，支持：
- 缓存配置
- 超时配置
- 健康检查配置
- 重试配置

### 4. CachedChatModel
缓存包装类，用于管理 ChatModel 实例的缓存。

## 使用方法

### 基本配置

在 `application.yml` 中添加配置：

```yaml
yxboot:
  llm:
    client:
      cache-enabled: true
      max-cache-size: 100
      cache-expire-minutes: 30
      default-timeout-seconds: 60
      health-check-enabled: true
      health-check-interval-minutes: 5
      retry:
        enabled: true
        max-attempts: 3
        delay-millis: 1000
        multiplier: 2.0
```

### 依赖注入

```java
@Service
@RequiredArgsConstructor
public class MyService {
    
    private final ChatClient chatClient;
    private final EnhancedChatClient enhancedChatClient;
    
    // 业务方法...
}
```

### 基本用法

#### 1. 简单文本对话

```java
public String simpleChat(Provider provider, String message) {
    return chatClient.chat(provider, message);
}
```

#### 2. 多轮对话

```java
public String multiTurnChat(Provider provider) {
    List<Message> messages = List.of(
        new SystemMessage("你是一个专业的助手"),
        new UserMessage("请介绍一下 Spring Boot")
    );
    return chatClient.chat(provider, messages);
}
```

#### 3. 带选项的对话

```java
public String chatWithOptions(Provider provider, String message) {
    ChatOptions options = ChatOptions.builder()
        .temperature(0.8f)
        .maxTokens(1000)
        .build();
    
    var response = chatClient.chatWithOptions(provider, message, options);
    return response.getContent();
}
```

#### 4. 流式对话

```java
public Flux<String> streamingChat(Provider provider, String message) {
    return chatClient.streamChat(provider, message)
        .doOnNext(chunk -> log.info("收到: {}", chunk));
}
```

### 高级用法

#### 1. 带重试的对话

```java
public String chatWithRetry(Provider provider, String message) {
    return enhancedChatClient.chatWithRetry(provider, message);
}
```

#### 2. 异步对话

```java
public void asyncChat(Provider provider, String message) {
    enhancedChatClient.chatAsync(provider, message)
        .subscribe(
            response -> log.info("回复: {}", response),
            error -> log.error("错误: {}", error.getMessage())
        );
}
```

#### 3. 批量处理

```java
public Flux<String> batchProcess(Provider provider, List<String> messages) {
    return enhancedChatClient.batchChat(provider, messages);
}
```

### 实际应用场景

#### 1. 智能客服

```java
public String customerService(Provider provider, String question, String context) {
    String systemPrompt = "你是专业客服，根据上下文回答问题：" + context;
    
    List<Message> messages = List.of(
        new SystemMessage(systemPrompt),
        new UserMessage(question)
    );
    
    ChatOptions options = ChatOptions.builder()
        .temperature(0.3f)
        .maxTokens(500)
        .build();
    
    var response = chatClient.chatWithOptions(provider, messages, options);
    return response.getContent();
}
```

#### 2. 代码生成

```java
public String generateCode(Provider provider, String requirement) {
    String systemPrompt = "你是程序员助手，生成高质量代码";
    
    List<Message> messages = List.of(
        new SystemMessage(systemPrompt),
        new UserMessage(requirement)
    );
    
    ChatOptions options = ChatOptions.builder()
        .temperature(0.2f)
        .maxTokens(2000)
        .build();
    
    var response = chatClient.chatWithOptions(provider, messages, options);
    return response.getContent();
}
```

#### 3. 文档总结

```java
public Flux<String> summarizeDocument(Provider provider, String document) {
    String systemPrompt = "你是文档分析师，请总结以下文档";
    
    List<Message> messages = List.of(
        new SystemMessage(systemPrompt),
        new UserMessage("文档内容：" + document)
    );
    
    ChatOptions options = ChatOptions.builder()
        .temperature(0.5f)
        .maxTokens(1500)
        .stream(true)
        .build();
    
    return chatClient.streamChatWithOptions(provider, messages, options)
        .map(response -> response.getContent());
}
```

## 缓存管理

### 缓存统计

```java
Map<String, Object> stats = chatClient.getCacheStats();
System.out.println("缓存大小: " + stats.get("cacheSize"));
```

### 清除缓存

```java
// 清除指定提供商缓存
chatClient.clearCache(provider);

// 清除所有缓存
chatClient.clearAllCache();
```

## 健康检查

### 检查提供商可用性

```java
boolean available = chatClient.isProviderAvailable(provider);
if (!available) {
    log.warn("提供商不可用: {}", provider.getProviderName());
}
```

### 获取健康状态

```java
Map<String, Object> healthStats = enhancedChatClient.getHealthStats();
System.out.println("健康的提供商数量: " + healthStats.get("healthyProviders"));
```

## 错误处理

### 基本错误处理

```java
try {
    String response = chatClient.chat(provider, message);
    return response;
} catch (Exception e) {
    log.error("对话失败: {}", e.getMessage(), e);
    throw new RuntimeException("对话服务暂时不可用", e);
}
```

### 流式错误处理

```java
return chatClient.streamChat(provider, message)
    .doOnError(error -> log.error("流式对话失败: {}", error.getMessage()))
    .onErrorReturn("抱歉，服务暂时不可用");
```

## 最佳实践

1. **合理配置缓存**：根据实际使用情况调整缓存大小和过期时间
2. **启用健康检查**：在生产环境中启用健康检查，及时发现问题
3. **使用重试机制**：对于重要的业务场景，使用带重试的方法
4. **异步处理**：对于不需要立即响应的场景，使用异步方法
5. **错误处理**：始终添加适当的错误处理逻辑
6. **日志记录**：记录关键操作的日志，便于问题排查
7. **参数调优**：根据不同场景调整温度、最大令牌数等参数

## 注意事项

1. **API 密钥安全**：确保 API 密钥的安全存储和传输
2. **并发控制**：注意并发访问时的性能和资源消耗
3. **成本控制**：合理使用缓存和批量处理来控制 API 调用成本
4. **监控告警**：建立适当的监控和告警机制
5. **版本兼容**：注意不同版本间的兼容性问题 