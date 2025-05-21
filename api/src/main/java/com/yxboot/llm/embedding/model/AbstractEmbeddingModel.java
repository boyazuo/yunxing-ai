package com.yxboot.llm.embedding.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private final int batchSize;

    /**
     * 线程池，用于并行处理大量嵌入请求
     */
    private final ExecutorService executorService;

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
     * 默认实现：调用单文本嵌入方法
     * 子类可以覆盖此方法以提供更高效的批处理实现
     */
    @Override
    public List<float[]> embedAll(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        // 如果文本数量小于等于批处理大小，直接调用批处理API
        if (texts.size() <= batchSize) {
            return embedBatch(texts);
        }

        // 将文本列表分成多个批次
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < texts.size(); i += batchSize) {
            batches.add(texts.subList(i, Math.min(i + batchSize, texts.size())));
        }

        // 使用CompletableFuture并行处理多个批次
        List<CompletableFuture<List<float[]>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> embedBatch(batch), executorService))
                .collect(Collectors.toList());

        // 等待所有批次处理完成并合并结果
        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
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
     * 关闭线程池
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}