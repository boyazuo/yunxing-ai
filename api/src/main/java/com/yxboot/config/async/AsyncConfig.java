package com.yxboot.config.async;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

/**
 * 异步配置类
 * 配置支持安全上下文传播的异步执行器
 * 
 * @author Boya
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 配置自定义异步执行器，支持Spring Security上下文传播
     * 
     * @return DelegatingSecurityContextAsyncTaskExecutor
     */
    @Bean("documentTaskExecutor")
    public Executor documentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数 - 可用处理器数量
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        // 最大线程数 - 可同时执行的任务数
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        // 队列容量 - 任务等待队列大小
        executor.setQueueCapacity(50);
        // 线程保持活跃时间 - 线程空闲时间
        executor.setKeepAliveSeconds(100);
        // 线程名称前缀
        executor.setThreadNamePrefix("document-task-");
        // 拒绝策略 - 当线程池达到最大线程数时，新任务的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待任务完成 - 在关闭线程池时，等待所有任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待终止时间 - 在关闭线程池时，等待任务完成的时间
        executor.setAwaitTerminationSeconds(60);
        // 初始化线程池
        executor.initialize();

        // 包装为支持安全上下文传播的执行器
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}