package com.yxboot.modules.ai.provider;

import java.io.Closeable;
import java.util.function.Consumer;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 服务器发送事件(SSE)流处理器
 * 用于处理模型的流式响应
 * 
 * @author Boya
 */
public interface SSEStreamHandler extends Closeable {

    /**
     * 处理SSE流，将结果发送给SseEmitter
     * 
     * @param emitter SSE发射器
     */
    void handle(SseEmitter emitter);

    /**
     * 处理SSE流，将结果回调给消费者
     * 
     * @param onMessage  消息消费者，接收每一条消息
     * @param onComplete 完成回调，在流结束时调用
     * @param onError    错误回调，在发生异常时调用
     */
    void handle(Consumer<String> onMessage, Runnable onComplete, Consumer<Throwable> onError);
}