package com.yxboot.util;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * SSE客户端工具类，用于处理Server-Sent Events流式请求
 * 
 * 重构说明： - 移除 @Component 注解，改为静态工具类 - 参考 HttpClient 的设计模式 - 提供静态方法进行 SSE 请求 - 支持自定义超时配置
 * 
 * @author Boya
 */
@Slf4j
public class SseClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int DEFAULT_CONNECT_TIMEOUT = 30;
    private static final int DEFAULT_READ_TIMEOUT = 60;
    private static final int DEFAULT_WRITE_TIMEOUT = 60;

    /**
     * 默认的OkHttpClient实例
     */
    private static final OkHttpClient DEFAULT_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build();

    /**
     * 创建自定义的OkHttpClient实例
     * 
     * @param connectTimeout 连接超时时间（秒）
     * @param readTimeout 读取超时时间（秒）
     * @param writeTimeout 写入超时时间（秒）
     * @return OkHttpClient实例
     */
    public static OkHttpClient customClient(int connectTimeout, int readTimeout, int writeTimeout) {
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 使用GET方法发起SSE请求
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @param eventHandler 事件处理器
     * @param errorHandler 错误处理器
     * @return Call对象，可用于取消请求
     */
    public static Call sseGet(String url, Map<String, String> headers, Consumer<String> eventHandler,
            Consumer<Throwable> errorHandler) {
        return sseRequest(url, null, headers, eventHandler, errorHandler, null, HttpMethod.GET);
    }

    /**
     * 使用POST方法发起SSE请求
     * 
     * @param url 请求URL
     * @param jsonBody 请求体（JSON格式）
     * @param headers 请求头
     * @param eventHandler 事件处理器
     * @param errorHandler 错误处理器
     * @return Call对象，可用于取消请求
     */
    public static Call ssePost(String url, String jsonBody, Map<String, String> headers, Consumer<String> eventHandler,
            Consumer<Throwable> errorHandler) {
        return sseRequest(url, jsonBody, headers, eventHandler, errorHandler, null, HttpMethod.POST);
    }

    /**
     * 使用自定义超时设置发起SSE请求
     * 
     * @param url 请求URL
     * @param jsonBody 请求体（JSON格式），GET请求可为null
     * @param headers 请求头
     * @param eventHandler 事件处理器
     * @param errorHandler 错误处理器
     * @param timeoutConfig 超时配置
     * @param method HTTP方法
     * @return Call对象，可用于取消请求
     */
    public static Call sseRequest(String url, String jsonBody, Map<String, String> headers,
            Consumer<String> eventHandler, Consumer<Throwable> errorHandler,
            TimeoutConfig timeoutConfig, HttpMethod method) {

        OkHttpClient client;
        if (timeoutConfig != null) {
            client = customClient(
                    timeoutConfig.getConnectTimeout(),
                    timeoutConfig.getReadTimeout(),
                    timeoutConfig.getWriteTimeout());
        } else {
            client = DEFAULT_CLIENT;
        }

        Request.Builder requestBuilder = new Request.Builder().url(url);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::addHeader);
        }

        // 设置请求方法和请求体
        if (method == HttpMethod.POST && jsonBody != null) {
            RequestBody body = RequestBody.create(jsonBody, JSON);
            requestBuilder.post(body);
        } else {
            requestBuilder.get();
        }

        Request request = requestBuilder.build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("SSE请求失败: {}", e.getMessage(), e);
                if (errorHandler != null) {
                    errorHandler.accept(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) {
                        String errorMsg = "SSE连接失败，状态码: " + response.code();
                        log.error(errorMsg);
                        if (errorHandler != null) {
                            errorHandler.accept(new IOException(errorMsg));
                        }
                        return;
                    }

                    // 处理SSE响应
                    try (okio.BufferedSource source = responseBody.source()) {
                        while (!call.isCanceled() && !Thread.currentThread().isInterrupted()) {
                            try {
                                // 尝试读取一行数据，超时会抛出IOException
                                String line = source.readUtf8Line();
                                if (line == null) {
                                    // 流结束
                                    break;
                                }

                                // 忽略空行或注释行
                                if (line.isEmpty() || line.startsWith(":")) {
                                    continue;
                                }

                                if (line.startsWith("data:")) {
                                    String data = line.substring(5).trim();
                                    // 将事件数据传递给处理器
                                    if (eventHandler != null) {
                                        eventHandler.accept(data);
                                    }
                                }
                            } catch (IOException e) {
                                log.error("读取SSE流数据异常: {}", e.getMessage(), e);
                                if (errorHandler != null) {
                                    errorHandler.accept(e);
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("处理SSE响应异常: {}", e.getMessage(), e);
                    if (errorHandler != null) {
                        errorHandler.accept(e);
                    }
                }
            }
        });

        return call;
    }

    /**
     * HTTP方法枚举
     */
    public enum HttpMethod {
        GET, POST
    }

    /**
     * 超时配置
     */
    @Data
    @Builder
    public static class TimeoutConfig {
        private int connectTimeout;
        private int readTimeout;
        private int writeTimeout;

        /**
         * 创建默认的超时配置
         * 
         * @return 默认超时配置
         */
        public static TimeoutConfig defaultConfig() {
            return TimeoutConfig.builder()
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
                    .readTimeout(DEFAULT_READ_TIMEOUT)
                    .writeTimeout(DEFAULT_WRITE_TIMEOUT)
                    .build();
        }

        /**
         * 创建自定义超时配置
         * 
         * @param timeout 所有超时统一设置（秒）
         * @return 自定义超时配置
         */
        public static TimeoutConfig of(int timeout) {
            return TimeoutConfig.builder()
                    .connectTimeout(timeout)
                    .readTimeout(timeout)
                    .writeTimeout(timeout)
                    .build();
        }
    }
}
