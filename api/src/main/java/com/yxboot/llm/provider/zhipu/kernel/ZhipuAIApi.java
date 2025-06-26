package com.yxboot.llm.provider.zhipu.kernel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import com.yxboot.util.HttpClient;
import com.yxboot.util.SseClient;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 智谱AI API客户端 封装了HTTP请求和响应处理逻辑
 * 
 * 重构说明： - 移除 @Component 注解，不再参与 IoC 管理 - 移除 Builder 模式，改为静态工具类 - 使用 Hutool JSONUtil 替换 Jackson
 * ObjectMapper - 设计为无状态静态工具类，与 HttpClient、SseClient 保持一致
 * 
 * @author Boya
 */
@Slf4j
public final class ZhipuAIApi {

    /**
     * 私有构造函数，防止实例化
     */
    private ZhipuAIApi() {
        throw new UnsupportedOperationException("ZhipuAIApi 是静态工具类，不允许实例化");
    }

    /**
     * 发送同步请求
     * 
     * @param <T> 响应类型
     * @param request 请求对象
     * @param apiKey API密钥
     * @param baseUrl API基础URL
     * @param responseType 响应类型Class
     * @return 响应对象
     */
    public static <T> T sendRequest(Object request, String apiKey, String baseUrl, Class<T> responseType) {
        // 序列化请求
        String jsonBody;
        try {
            jsonBody = JSONUtil.toJsonStr(request);
        } catch (Exception e) {
            log.error("序列化请求体失败: {}", e.getMessage(), e);
            throw new RuntimeException("序列化请求体失败", e);
        }

        // 构建请求头
        Map<String, String> headers = buildHeaders(apiKey);

        // 发送请求
        String response = HttpClient.postJson(baseUrl, jsonBody, headers);

        // 解析响应
        try {
            return JSONUtil.toBean(response, responseType);
        } catch (Exception e) {
            log.error("解析响应失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析响应失败", e);
        }
    }

    /**
     * 发送流式请求
     * 
     * @param request 请求对象
     * @param apiKey API密钥
     * @param baseUrl API基础URL
     * @param dataProcessor 数据处理函数
     * @param errorHandler 错误处理函数
     * @return 文本流
     */
    public static Flux<String> sendStreamRequest(Object request, String apiKey, String baseUrl,
            StreamDataProcessor dataProcessor, Consumer<Throwable> errorHandler) {
        // 序列化请求
        String jsonBody;
        try {
            jsonBody = JSONUtil.toJsonStr(request);
        } catch (Exception e) {
            log.error("序列化请求体失败: {}", e.getMessage(), e);
            return Flux.error(e);
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
                    String content = dataProcessor.process(data);
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

            // 发起SSE请求 - 使用静态方法调用
            SseClient.ssePost(baseUrl, jsonBody, headers, eventHandler, onError);
        });
    }

    /**
     * 构建请求头
     * 
     * @param apiKey API密钥
     * @return 请求头Map
     */
    private static Map<String, String> buildHeaders(String apiKey) {
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
         * @param data JSON格式的数据
         * @return 提取的内容
         * @throws Exception 处理异常
         */
        String process(String data) throws Exception;
    }
}
