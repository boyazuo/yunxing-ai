package com.yxboot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * HTTP请求工具类，基于OkHttpClient实现
 */
@Slf4j
public class HttpClient {

    /**
     * JSON格式MediaType
     */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * OkHttpClient实例
     */
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build();

    /**
     * GET请求
     *
     * @param url     请求URL
     * @param headers 请求头
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        addHeaders(builder, headers);
        return executeRequest(builder.build());
    }

    /**
     * GET请求（无请求头）
     *
     * @param url 请求URL
     * @return 响应内容
     */
    public static String get(String url) {
        return get(url, null);
    }

    /**
     * POST请求（JSON格式请求体）
     *
     * @param url     请求URL
     * @param json    JSON格式请求体
     * @param headers 请求头
     * @return 响应内容
     */
    public static String postJson(String url, String json, Map<String, String> headers) {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        addHeaders(builder, headers);
        return executeRequest(builder.build());
    }

    /**
     * POST请求（JSON格式请求体，无请求头）
     *
     * @param url  请求URL
     * @param json JSON格式请求体
     * @return 响应内容
     */
    public static String postJson(String url, String json) {
        return postJson(url, json, null);
    }

    /**
     * POST请求（表单格式请求体）
     *
     * @param url     请求URL
     * @param params  表单参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String postForm(String url, Map<String, String> params, Map<String, String> headers) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            params.forEach(formBuilder::add);
        }
        RequestBody body = formBuilder.build();
        Request.Builder builder = new Request.Builder().url(url).post(body);
        addHeaders(builder, headers);
        return executeRequest(builder.build());
    }

    /**
     * POST请求（表单格式请求体，无请求头）
     *
     * @param url    请求URL
     * @param params 表单参数
     * @return 响应内容
     */
    public static String postForm(String url, Map<String, String> params) {
        return postForm(url, params, null);
    }

    /**
     * PUT请求（JSON格式请求体）
     *
     * @param url     请求URL
     * @param json    JSON格式请求体
     * @param headers 请求头
     * @return 响应内容
     */
    public static String putJson(String url, String json, Map<String, String> headers) {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder().url(url).put(body);
        addHeaders(builder, headers);
        return executeRequest(builder.build());
    }

    /**
     * PUT请求（JSON格式请求体，无请求头）
     *
     * @param url  请求URL
     * @param json JSON格式请求体
     * @return 响应内容
     */
    public static String putJson(String url, String json) {
        return putJson(url, json, null);
    }

    /**
     * DELETE请求
     *
     * @param url     请求URL
     * @param headers 请求头
     * @return 响应内容
     */
    public static String delete(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url).delete();
        addHeaders(builder, headers);
        return executeRequest(builder.build());
    }

    /**
     * DELETE请求（无请求头）
     *
     * @param url 请求URL
     * @return 响应内容
     */
    public static String delete(String url) {
        return delete(url, null);
    }

    /**
     * 下载文件
     * 
     * @param url        文件URL
     * @param targetFile 目标文件
     * @param headers    请求头
     * @return 是否下载成功
     */
    public static boolean downloadFile(String url, File targetFile, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        addHeaders(builder, headers);
        Request request = builder.build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("文件下载失败: {}, 状态码: {}", url, response.code());
                return false;
            }

            // 确保目标文件的父目录存在
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    log.error("创建目录失败: {}", parentDir.getAbsolutePath());
                    return false;
                }
            }

            // 将响应内容写入文件
            try (InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                    FileOutputStream outputStream = new FileOutputStream(targetFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                long contentLength = response.body().contentLength();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // 日志记录下载进度
                    if (contentLength > 0) {
                        int progress = (int) (totalBytesRead * 100 / contentLength);
                        log.debug("下载进度: {}%, {}/{} bytes", progress, totalBytesRead, contentLength);
                    }
                }

                outputStream.flush();
                log.info("文件下载完成: {}, 大小: {} bytes", targetFile.getAbsolutePath(), totalBytesRead);
                return true;
            }
        } catch (IOException e) {
            log.error("文件下载异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 下载文件（无请求头）
     * 
     * @param url        文件URL
     * @param targetFile 目标文件
     * @return 是否下载成功
     */
    public static boolean downloadFile(String url, File targetFile) {
        return downloadFile(url, targetFile, null);
    }

    /**
     * 自定义OkHttpClient
     *
     * @param connectTimeout 连接超时时间（秒）
     * @param readTimeout    读取超时时间（秒）
     * @param writeTimeout   写入超时时间（秒）
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
     * 使用自定义OkHttpClient执行请求
     *
     * @param client  OkHttpClient实例
     * @param request 请求对象
     * @return 响应内容
     */
    public static String executeRequest(OkHttpClient client, Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("HTTP请求失败: {}, 状态码: {}", request.url(), response.code());
                throw new IOException("HTTP请求失败: " + response.code());
            }
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            log.error("HTTP请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("HTTP请求异常", e);
        }
    }

    /**
     * 执行请求
     *
     * @param request 请求对象
     * @return 响应内容
     */
    private static String executeRequest(Request request) {
        return executeRequest(CLIENT, request);
    }

    /**
     * 添加请求头
     *
     * @param builder 请求构建器
     * @param headers 请求头
     */
    private static void addHeaders(Request.Builder builder, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((key, value) -> {
                if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                    builder.addHeader(key, value);
                }
            });
        }
    }
}
