package com.yxboot.llm.provider.zhipu.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxboot.util.HttpClient;
import com.yxboot.util.SseClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 知启API客户端
 * 封装了HTTP请求和响应处理逻辑
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZhipuClient {

    private final ObjectMapper objectMapper;
    private final SseClient sseClient;

    /**
     * 发送同步请求
     * 
     * @param <T>          响应类型
     * @param request      请求对象
     * @param apiKey       API密钥
     * @param baseUrl      API基础URL
     * @param responseType 响应类型Class
     * @return 响应对象
     */
    public <T> T sendRequest(Object request, String apiKey, String baseUrl, Class<T> responseType) {
        // 序列化请求
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("序列化请求体失败: {}", e.getMessage(), e);
            throw new RuntimeException("序列化请求体失败", e);
        }

        // 构建请求头
        Map<String, String> headers = buildHeaders(apiKey);

        // 发送请求
        String response = HttpClient.postJson(baseUrl, jsonBody, headers);

        // 解析响应
        try {
            return objectMapper.readValue(response, responseType);
        } catch (JsonProcessingException e) {
            log.error("解析响应失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析响应失败", e);
        }
    }

    /**
     * 发送流式请求
     * 
     * @param request       请求对象
     * @param apiKey        API密钥
     * @param baseUrl       API基础URL
     * @param dataProcessor 数据处理函数
     * @param errorHandler  错误处理函数
     * @return 文本流
     */
    public Flux<String> sendStreamRequest(Object request, String apiKey, String baseUrl,
            StreamDataProcessor dataProcessor, Consumer<Throwable> errorHandler) {
        // 序列化请求
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("序列化请求体失败: {}", e.getMessage(), e);
            return Flux.error(new RuntimeException("序列化请求体失败", e));
        }

        // 构建请求头
        Map<String, String> headers = buildHeaders(apiKey);

        return Flux.create(sink -> {
            // 定义事件处理器
            Consumer<String> eventHandler = data -> {
                if ("[DONE]".equals(data)) {
                    // 流结束
                    sink.complete();
                    return;
                }

                try {
                    // 处理数据
                    String content = dataProcessor.process(data, objectMapper);
                    if (content != null && !content.isEmpty()) {
                        sink.next(content);
                    }
                } catch (Exception e) {
                    log.error("处理流数据失败: {}", e.getMessage(), e);
                    sink.error(new RuntimeException("处理流数据失败", e));
                }
            };

            // 定义错误处理器
            Consumer<Throwable> onError = error -> {
                log.error("流请求失败: {}", error.getMessage(), error);
                if (errorHandler != null) {
                    errorHandler.accept(error);
                }
                sink.error(error);
            };

            // 发起SSE请求
            sseClient.ssePost(baseUrl, jsonBody, headers, eventHandler, onError);
        });
    }

    /**
     * 构建请求头
     * 
     * @param apiKey API密钥
     * @return 请求头Map
     */
    private Map<String, String> buildHeaders(String apiKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        return headers;
    }

    /**
     * 流数据处理器接口
     */
    @FunctionalInterface
    public interface StreamDataProcessor {
        /**
         * 处理流数据
         * 
         * @param data         JSON格式的数据
         * @param objectMapper JSON处理器
         * @return 提取的内容
         * @throws Exception 处理异常
         */
        String process(String data, ObjectMapper objectMapper) throws Exception;
    }
}