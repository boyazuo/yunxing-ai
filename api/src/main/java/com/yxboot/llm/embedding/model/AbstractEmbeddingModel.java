package com.yxboot.llm.embedding.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yxboot.llm.embedding.config.EmbeddingConfig;
import com.yxboot.llm.embedding.model.EmbeddingResponse.EmbeddingResult;

/**
 * 抽象嵌入模型实现，提供基础功能
 */
public abstract class AbstractEmbeddingModel implements EmbeddingModel {

    /**
     * 默认线程池大小
     */
    private static final int DEFAULT_THREAD_POOL_SIZE = 5;

    /**
     * 批处理大小，每次API调用最多处理的文本数量
     */
    protected int batchSize;

    /**
     * 线程池，用于并行处理大量嵌入请求
     */
    protected final ExecutorService executorService;

    /**
     * 构造函数
     */
    public AbstractEmbeddingModel() {
        this(32, DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * 构造函数
     *
     * @param batchSize 批处理大小
     */
    public AbstractEmbeddingModel(int batchSize) {
        this(batchSize, DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * 构造函数
     *
     * @param batchSize      批处理大小
     * @param threadPoolSize 线程池大小
     */
    public AbstractEmbeddingModel(int batchSize, int threadPoolSize) {
        this.batchSize = batchSize;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * 配置方法的默认实现
     * 子类应该覆盖此方法以提供自定义实现
     *
     * @param config 嵌入配置
     */
    @Override
    public void configure(EmbeddingConfig config) {
        if (config != null) {
            this.batchSize = config.getBatchSize();
        }
    }

    /**
     * 设置API密钥的默认实现
     * 子类应该覆盖此方法以提供实际的API密钥设置逻辑
     *
     * @param apiKey API密钥
     * @return 当前模型实例
     */
    @Override
    public EmbeddingModel withApiKey(String apiKey) {
        // 默认实现：什么都不做，子类应该覆盖此方法
        return this;
    }

    /**
     * 默认实现：调用单文本嵌入方法
     * 子类可以覆盖此方法以提供更高效的批处理实现
     */
    @Override
    public List<float[]> embedAll(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        if (texts.size() == 1) {
            List<float[]> result = new ArrayList<>(1);
            result.add(embed(texts.get(0)));
            return result;
        }

        // 分批处理大量文本
        if (texts.size() > batchSize) {
            return processBatches(texts);
        }

        // 处理单个批次
        return embedBatch(texts);
    }

    /**
     * 分批处理大量文本
     *
     * @param texts 文本列表
     * @return 向量列表
     */
    protected List<float[]> processBatches(List<String> texts) {
        int totalSize = texts.size();
        int batchCount = (totalSize + batchSize - 1) / batchSize; // 向上取整

        List<CompletableFuture<List<float[]>>> futures = new ArrayList<>(batchCount);

        // 将文本分批并提交到线程池
        for (int i = 0; i < batchCount; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, totalSize);
            List<String> batch = texts.subList(start, end);

            // 创建异步任务
            CompletableFuture<List<float[]>> future = CompletableFuture.supplyAsync(
                    () -> embedBatch(batch),
                    executorService);

            futures.add(future);
        }

        // 等待所有批次完成并合并结果
        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public EmbeddingResponse embedRequest(EmbeddingRequest request) {
        if (request == null || request.getInput() == null || request.getInput().isEmpty()) {
            return EmbeddingResponse.builder()
                    .modelName(getModelName())
                    .build();
        }

        // 使用现有的embedAll方法处理文本
        List<float[]> embeddings = embedAll(request.getInput());

        // 创建嵌入结果列表
        List<EmbeddingResult> results = IntStream.range(0, embeddings.size())
                .mapToObj(i -> EmbeddingResult.builder()
                        .index(i)
                        .object("embedding")
                        .embedding(embeddings.get(i))
                        .build())
                .collect(Collectors.toList());

        // 构建响应
        return EmbeddingResponse.builder()
                .modelName(getModelName())
                .data(results)
                .tokenUsage(EmbeddingResponse.TokenUsage.of(calculateTokens(request.getInput())))
                .build();
    }

    /**
     * 单个批次的嵌入处理
     * 子类应该覆盖此方法以提供实际的批处理实现
     *
     * @param batch 文本批次
     * @return 向量列表
     */
    protected List<float[]> embedBatch(List<String> batch) {
        // 默认实现：顺序处理每个文本
        return batch.stream()
                .map(this::embed)
                .collect(Collectors.toList());
    }

    /**
     * 计算输入文本的token数量
     * 默认实现使用简单的空格分词估算
     * 子类可以覆盖此方法提供更准确的计算
     * 
     * @param texts 输入文本列表
     * @return 估算的token数量
     */
    protected int calculateTokens(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return 0;
        }

        // 简单估算：按空格分词，每个单词算作一个token
        return texts.stream()
                .mapToInt(text -> text.split("\\s+").length)
                .sum();
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}