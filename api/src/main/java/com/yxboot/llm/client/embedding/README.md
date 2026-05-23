# EmbeddingClient 使用指南

## 概述

EmbeddingClient 是云行 AI 系统中连接通用封装层（EmbeddingModel）和业务层的桥梁，提供简化的文本嵌入 API 调用接口，隐藏底层复杂性。

## 重构优化

### 主要改进

1. **参考 ChatModel 架构**：采用与 ChatModel 一致的设计模式
2. **Builder 模式**：使用 Builder 模式创建不可变的模型实例
3. **简化工厂**：移除复杂的缓存和依赖注入，每次创建新实例
4. **统一接口**：提供一致的 API 调用方式

### 核心变化

- `ZhipuAIEmbeddingModel` 现在是不可变对象，只能通过 Builder 创建
- `EmbeddingModelFactory` 简化为类似 `ChatModelFactory` 的设计
- 移除了 `configure()` 和 `withApiKey()` 等可变方法
- 统一使用 switch 表达式提高代码可读性

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

### 1. EmbeddingModel（重构后）
```java
// 新的创建方式：使用 Builder 模式
ZhipuAIEmbeddingModel model = ZhipuAIEmbeddingModel.builder()
    .apiKey("your-api-key")
    .modelName("embedding-3")
    .embeddingDimension(2048)
    .batchSize(32)
    .build();

// 或者使用配置对象
ZhipuAIEmbeddingConfig config = ZhipuAIEmbeddingConfig.builder()
    .apiKey("your-api-key")
    .modelName("embedding-3")
    .build();

ZhipuAIEmbeddingModel model = ZhipuAIEmbeddingModel.builder()
    .config(config)
    .build();
```

### 2. EmbeddingModelFactory（重构后）
```java
// 简化的工厂方法
EmbeddingModel model = embeddingModelFactory.createEmbeddingModel(provider, modelInfo);

// 或者使用默认模型
EmbeddingModel model = embeddingModelFactory.createEmbeddingModel(provider);

// 检查支持的提供商
boolean supported = embeddingModelFactory.isProviderSupported("zhipuai");
List<String> providers = embeddingModelFactory.getSupportedProviders();
```

### 3. EmbeddingClient
基础嵌入客户端，提供核心的文本嵌入功能：
- 简单文本嵌入
- 批量文本嵌入
- 带选项的嵌入处理
- 缓存管理
- 健康检查

### 4. EmbeddingClientConfig
配置管理类，支持：
- 缓存配置
- 超时配置
- 健康检查配置
- 重试配置

## 使用方法

### 基本配置

在 `application.yml` 中添加配置：

```yaml
yxboot:
  llm:
    embedding:
      client:
        cache-enabled: true
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
    private final EmbeddingModelFactory embeddingModelFactory;
    
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

#### 3. 带模型信息的嵌入

```java
public EmbeddingResponse embedWithModel(Provider provider, Model model, List<String> texts) {
    EmbeddingRequest request = EmbeddingRequest.of(texts);
    return embeddingClient.embedRequest(provider, model, request);
}
```

#### 4. 直接使用模型

```java
public float[] directEmbed(Provider provider, String text) {
    // 创建模型实例
    EmbeddingModel model = embeddingModelFactory.createEmbeddingModel(provider);
    
    // 执行嵌入
    return model.embed(text);
}
```

### 高级用法

#### 1. 自定义配置

```java
// 创建自定义配置
ZhipuAIEmbeddingConfig customConfig = ZhipuAIEmbeddingConfig.builder()
    .apiKey("your-api-key")
    .modelName("embedding-3")
    .embeddingDimension(1024)  // 自定义维度
    .batchSize(16)             // 自定义批处理大小
    .build();

// 使用自定义配置创建模型
ZhipuAIEmbeddingModel model = ZhipuAIEmbeddingModel.builder()
    .config(customConfig)
    .build();
```

#### 2. 健康检查

```java
public boolean checkProviderHealth(Provider provider) {
    return embeddingClient.isProviderAvailable(provider);
}
```

#### 3. 缓存管理

```java
// 清除特定提供商缓存
embeddingClient.clearProviderCache(provider);

// 清除所有缓存
embeddingClient.clearAllCache();

// 获取缓存统计
Map<String, Object> stats = embeddingClient.getCacheStats();
```

## 最佳实践

### 1. 配置不可变性
```java
// ✅ 正确：使用 Builder 模式创建新实例
ZhipuAIEmbeddingConfig config = originalConfig
    .withModelName("new-model")
    .withEmbeddingDimension(1024);

// ❌ 错误：尝试修改现有实例（已废弃）
// model.configure(newConfig);  // 抛出 UnsupportedOperationException
// model.withApiKey(newKey);    // 抛出 UnsupportedOperationException
```

### 2. 工厂模式使用
```java
// ✅ 正确：每次创建新实例
EmbeddingModel model1 = factory.createEmbeddingModel(provider1);
EmbeddingModel model2 = factory.createEmbeddingModel(provider2);

// ✅ 正确：检查支持的提供商
if (factory.isProviderSupported(providerName)) {
    EmbeddingModel model = factory.createEmbeddingModel(provider);
}
```

### 3. 错误处理
```java
public float[] safeEmbed(Provider provider, String text) {
    try {
        return embeddingClient.embed(provider, text);
    } catch (UnsupportedOperationException e) {
        log.error("不支持的提供商: {}", provider.getProviderName());
        throw new ServiceException("嵌入服务不可用", e);
    } catch (Exception e) {
        log.error("嵌入处理失败", e);
        throw new ServiceException("嵌入处理失败", e);
    }
}
```

### 4. 性能优化
```java
// 利用缓存机制
public List<float[]> optimizedBatchEmbed(Provider provider, List<String> texts) {
    // EmbeddingClient 会自动缓存模型实例
    // 多次调用会复用同一个模型实例
    return embeddingClient.embedAll(provider, texts);
}
```

## 迁移指南

### 从旧版本迁移

#### 1. 模型创建方式变更
```java
// 旧方式（已废弃）
ZhipuAIEmbeddingModel model = new ZhipuAIEmbeddingModel();
model.configure(config);
model.withApiKey(apiKey);

// 新方式
ZhipuAIEmbeddingModel model = ZhipuAIEmbeddingModel.builder()
    .apiKey(apiKey)
    .modelName("embedding-3")
    .build();
```

#### 2. 工厂使用方式变更
```java
// 旧方式（复杂缓存）
EmbeddingModel model = factory.createEmbeddingModel(provider, modelInfo);
// 内部有复杂的缓存逻辑和状态管理

// 新方式（简化）
EmbeddingModel model = factory.createEmbeddingModel(provider, modelInfo);
// 每次创建新实例，缓存由 EmbeddingClient 管理
```

## 注意事项

1. **不可变性**：所有模型实例都是不可变的，不要尝试修改它们的状态
2. **线程安全**：重构后的所有组件都是线程安全的
3. **内存管理**：适当使用缓存清理机制，避免内存泄漏
4. **错误处理**：始终处理 `UnsupportedOperationException` 异常
5. **配置验证**：创建模型时会自动验证必要的配置参数

## 支持的提供商

当前支持的提供商：
- ZhipuAI (`zhipuai`, `zhipu`)

计划支持的提供商：
- QianWen (`qianwen`, `qwen`, `tongyi`)
- OpenAI (`openai`, `open_ai`)

## 故障排除

### 常见问题

1. **`UnsupportedOperationException`**：尝试调用已废弃的方法
   - 解决方案：使用 Builder 模式创建新实例

2. **`IllegalArgumentException`**：配置参数无效
   - 解决方案：检查 API 密钥和模型名称是否正确

3. **缓存过期**：模型实例失效
   - 解决方案：检查缓存配置，适当调整过期时间

### 调试技巧

1. 开启调试日志：
```yaml
logging:
  level:
    com.yxboot.llm: DEBUG
```

2. 监控缓存状态：
```java
Map<String, Object> stats = embeddingClient.getCacheStats();
log.info("缓存统计: {}", stats);
``` 