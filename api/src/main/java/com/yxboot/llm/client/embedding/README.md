# EmbeddingClient 使用指南

## 概述

EmbeddingClient 是云行 AI 系统中连接通用封装层（EmbeddingModel）和业务层的桥梁，提供简化的文本嵌入 API 调用接口，隐藏底层复杂性。

## 架构层次

```
1. 大模型原生 API (如 ZhipuAIApi)
   ↓
2. 通用封装层 (EmbeddingModel)
   ↓
3. 连接层 (EmbeddingClient) ← 当前组件
   ↓
4. 业务逻辑层 (上层应用的业务层)
```

## 核心组件

### 1. EmbeddingClient
基础嵌入客户端，提供核心的文本嵌入功能：
- 简单文本嵌入
- 批量文本嵌入
- 带选项的嵌入处理
- 缓存管理
- 健康检查

### 2. EnhancedEmbeddingClient
增强版嵌入客户端，在基础功能上添加：
- 重试机制
- 异步处理
- 批量处理多个提供商
- 定时健康检查
- 自动缓存清理

### 3. EmbeddingClientConfig
配置管理类，支持：
- 缓存配置
- 超时配置
- 健康检查配置
- 重试配置

### 4. CachedEmbeddingModel
缓存包装类，用于管理 EmbeddingModel 实例的缓存。

## 使用方法

### 基本配置

在 `application.yml` 中添加配置：

```yaml
yxboot:
  llm:
    embedding:
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
    
    private final EmbeddingClient embeddingClient;
    private final EnhancedEmbeddingClient enhancedEmbeddingClient;
    
    // 业务方法...
}
```

### 基本用法

#### 1. 简单文本嵌入

```java
public float[] simpleEmbed(Provider provider, String text) {
    return embeddingClient.embed(provider, text);
}
```

#### 2. 批量文本嵌入

```java
public List<float[]> batchEmbed(Provider provider, List<String> texts) {
    return embeddingClient.embedAll(provider, texts);
}
```

#### 3. 带模型名称的嵌入

```java
public float[] embedWithModel(Provider provider, String text, String modelName) {
    return embeddingClient.embedWithModel(provider, text, modelName);
}
```

#### 4. 带选项的嵌入处理

```java
public EmbeddingResponse embedWithOptions(Provider provider, List<String> texts, 
                                        String modelName, Integer dimensions) {
    Map<String, Object> options = new HashMap<>();
    options.put("encoding_format", "float");
    
    return embeddingClient.embedWithOptions(provider, texts, modelName, dimensions, options);
}
```

### 高级用法

#### 1. 带重试的嵌入处理

```java
public float[] embedWithRetry(Provider provider, String text) {
    return enhancedEmbeddingClient.embedWithRetry(provider, text);
}
```

#### 2. 异步文本嵌入

```java
public Mono<float[]> embedAsync(Provider provider, String text) {
    return enhancedEmbeddingClient.embedAsync(provider, text);
}
```

#### 3. 批量处理多个提供商

```java
public Map<String, EmbeddingResponse> batchEmbedMultipleProviders(
        Map<Provider, EmbeddingRequest> requests) {
    return enhancedEmbeddingClient.batchEmbedRequest(requests);
}
```

### 健康检查和监控

#### 1. 单个提供商健康检查

```java
public boolean checkHealth(Provider provider) {
    return embeddingClient.healthCheck(provider);
}
```

#### 2. 获取所有提供商健康状态

```java
public Map<String, Boolean> getAllHealthStatus() {
    return enhancedEmbeddingClient.getAllHealthStatus();
}
```

#### 3. 获取缓存统计信息

```java
public Map<String, Object> getCacheStats() {
    return embeddingClient.getCacheStats();
}
```

### 缓存管理

#### 1. 预热缓存

```java
public void warmupCache(List<Provider> providers) {
    embeddingClient.warmupCache(providers);
}
```

#### 2. 清除特定提供商缓存

```java
public void clearProviderCache(Provider provider) {
    embeddingClient.clearCache(provider);
}
```

#### 3. 清除所有缓存

```java
public void clearAllCache() {
    embeddingClient.clearAllCache();
}
```

## 实际应用场景

### 1. 文档相似度计算

```java
public double calculateSimilarity(Provider provider, String text1, String text2) {
    float[] embedding1 = embeddingClient.embed(provider, text1);
    float[] embedding2 = embeddingClient.embed(provider, text2);
    
    return cosineSimilarity(embedding1, embedding2);
}
```

### 2. 知识库检索

```java
public List<String> searchKnowledgeBase(Provider provider, String query, List<String> documents) {
    // 获取查询嵌入向量
    float[] queryEmbedding = embeddingClient.embed(provider, query);
    
    // 批量获取文档嵌入向量
    List<float[]> documentEmbeddings = embeddingClient.embedAll(provider, documents);
    
    // 计算相似度并排序返回最相似的文档
    // ... 相似度计算逻辑
}
```

### 3. 文本聚类

```java
public Map<String, List<String>> clusterTexts(Provider provider, List<String> texts) {
    // 批量获取文本嵌入向量
    List<float[]> embeddings = embeddingClient.embedAll(provider, texts);
    
    // 使用聚类算法对向量进行聚类
    // ... 聚类逻辑
}
```

## 错误处理

### 1. 基本错误处理

```java
try {
    float[] embedding = embeddingClient.embed(provider, text);
    // 处理成功结果
} catch (Exception e) {
    log.error("嵌入处理失败: {}", e.getMessage());
    // 错误处理逻辑
}
```

### 2. 异步错误处理

```java
embeddingClient.embedAsync(provider, text)
    .doOnSuccess(embedding -> {
        // 处理成功结果
    })
    .doOnError(error -> {
        log.error("异步嵌入失败: {}", error.getMessage());
        // 错误处理逻辑
    })
    .subscribe();
```

## 性能优化建议

### 1. 批量处理
- 尽量使用 `embedAll()` 进行批量处理，而不是多次调用 `embed()`
- 合理设置批处理大小，避免单次请求过大

### 2. 缓存策略
- 启用缓存以避免重复创建 EmbeddingModel 实例
- 根据业务需求调整缓存过期时间
- 定期清理过期缓存

### 3. 异步处理
- 对于大量文本处理，使用异步方法提高并发性能
- 合理设置线程池大小

### 4. 重试机制
- 启用重试机制处理临时网络问题
- 合理设置重试次数和延迟时间

## 监控和日志

### 1. 关键指标监控
- 嵌入处理成功率
- 平均响应时间
- 缓存命中率
- 提供商健康状态

### 2. 日志配置

```yaml
logging:
  level:
    com.yxboot.llm.client.embedding: DEBUG
    com.yxboot.llm.embedding: INFO
```

## 注意事项

1. **API 密钥安全**：确保 Provider 中的 API 密钥安全存储
2. **并发控制**：注意控制并发请求数量，避免超出 API 限制
3. **内存管理**：大量向量数据可能占用较多内存，注意内存使用情况
4. **网络超时**：合理设置网络超时时间，避免长时间等待
5. **错误重试**：对于临时性错误，使用重试机制；对于永久性错误，及时停止重试

## 扩展开发

如需添加新的嵌入模型提供商：

1. 实现 `EmbeddingModel` 接口
2. 在 `EmbeddingModelFactory` 中添加对应的创建逻辑
3. 添加相应的配置类
4. 更新文档和示例代码 