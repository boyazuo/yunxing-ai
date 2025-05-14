package com.yxboot.modules.ai.provider.qianwen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yxboot.modules.ai.dto.ChatMessageDTO;
import com.yxboot.modules.ai.dto.ModelRequestDTO;
import com.yxboot.modules.ai.dto.ModelResponseDTO;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.provider.ModelProvider;
import com.yxboot.modules.ai.provider.SSEStreamHandler;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 阿里云通义千问模型提供商实现
 * 
 * @author Boya
 */
@Slf4j
@Component
public class QianWenProvider implements ModelProvider {

    private static final String DEFAULT_QIANWEN_HOST = "https://dashscope.aliyuncs.com";
    private static final String API_PATH = "/api/v1/services/aigc/text-generation/generation";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final long DEFAULT_TIMEOUT = 60;

    private final OkHttpClient httpClient;

    public QianWenProvider() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getProviderName() {
        return "Qianwen";
    }

    @Override
    public boolean supports(Provider provider, Model model) {
        return "Qianwen".equalsIgnoreCase(provider.getProviderName());
    }

    @Override
    public ModelResponseDTO chatCompletion(Provider provider, Model model, ModelRequestDTO request)
            throws IOException {
        String apiKey = provider.getApiKey();
        String baseUrl = provider.getEndpoint();

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = DEFAULT_QIANWEN_HOST;
        }

        String requestJson = buildRequestJson(model, request);

        Request httpRequest = new Request.Builder()
                .url(baseUrl + API_PATH)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, JSON))
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("响应体为空");
            }

            String responseBody = body.string();
            log.debug("通义千问响应: {}", responseBody);

            // 这里简化处理，实际需要解析JSON响应
            QianWenResponse qianWenResponse = parseResponse(responseBody);

            return ModelResponseDTO.builder()
                    .content(qianWenResponse.getOutput())
                    .model(model.getModelName())
                    .requestId(qianWenResponse.getRequestId())
                    .build();
        }
    }

    @Override
    public SSEStreamHandler streamingChatCompletion(Provider provider, Model model, ModelRequestDTO request)
            throws IOException {
        String apiKey = provider.getApiKey();
        String baseUrl = provider.getEndpoint();

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = DEFAULT_QIANWEN_HOST;
        }

        // 设置流式请求参数
        request.setStream(true);
        String requestJson = buildRequestJson(model, request);

        Request httpRequest = new Request.Builder()
                .url(baseUrl + API_PATH)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .post(RequestBody.create(requestJson, JSON))
                .build();

        return new QianWenStreamHandler(httpClient, httpRequest);
    }

    private String buildRequestJson(Model model, ModelRequestDTO request) {
        List<QianWenMessage> messages = new ArrayList<>();

        // 添加聊天历史
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (ChatMessageDTO msg : request.getMessages()) {
                messages.add(new QianWenMessage(msg.getRole(), msg.getContent()));
            }
        } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
            // 如果没有消息但有提示，将提示作为用户消息
            messages.add(new QianWenMessage("user", request.getPrompt()));
        }

        // 构建请求JSON，简化处理
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\":\"").append(model.getModelName()).append("\",");
        json.append("\"input\":{");
        json.append("\"messages\":[");

        // 处理系统消息
        if (request.getSystemMessage() != null && !request.getSystemMessage().isEmpty()) {
            json.append("{\"role\":\"system\",\"content\":\"").append(request.getSystemMessage()).append("\"},");
        }

        // 添加消息
        for (int i = 0; i < messages.size(); i++) {
            QianWenMessage msg = messages.get(i);
            json.append("{\"role\":\"").append(msg.getRole()).append("\",");
            json.append("\"content\":\"").append(msg.getContent()).append("\"}");
            if (i < messages.size() - 1) {
                json.append(",");
            }
        }

        json.append("]},");
        json.append("\"parameters\":{");
        json.append("\"temperature\":").append(request.getTemperature()).append(",");
        if (request.getMaxTokens() != null) {
            json.append("\"max_tokens\":").append(request.getMaxTokens()).append(",");
        } else if (model.getMaxTokens() != null) {
            json.append("\"max_tokens\":").append(model.getMaxTokens()).append(",");
        }
        json.append("\"top_p\":").append(request.getTopP()).append(",");
        json.append("\"stream\":").append(request.getStream());
        json.append("}");
        json.append("}");
        json.append("}");

        return json.toString();
    }

    private QianWenResponse parseResponse(String responseBody) {
        // 简化处理，实际需要使用Jackson等库解析JSON
        QianWenResponse response = new QianWenResponse();
        response.setOutput("这是通义千问的回复内容");
        response.setRequestId("qianwen-request-id");
        return response;
    }

    /**
     * 通义千问消息
     */
    private static class QianWenMessage {
        private String role;
        private String content;

        public QianWenMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    /**
     * 通义千问响应
     */
    private static class QianWenResponse {
        private String output;
        private String requestId;

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }
    }

    /**
     * 通义千问流处理器
     */
    private static class QianWenStreamHandler implements SSEStreamHandler {
        private final OkHttpClient client;
        private final Request request;
        private okhttp3.Call call;

        public QianWenStreamHandler(OkHttpClient client, Request request) {
            this.client = client;
            this.request = request;
        }

        @Override
        public void handle(SseEmitter emitter) {
            handle(
                    message -> {
                        try {
                            emitter.send(message);
                        } catch (Exception e) {
                            log.error("发送SSE消息失败", e);
                        }
                    },
                    () -> emitter.complete(),
                    error -> {
                        log.error("SSE流处理异常", error);
                        emitter.completeWithError(error);
                    });
        }

        @Override
        public void handle(Consumer<String> onMessage, Runnable onComplete, Consumer<Throwable> onError) {
            call = client.newCall(request);

            try {
                Response response = call.execute();
                if (!response.isSuccessful()) {
                    throw new IOException("请求失败: " + response.code() + " " + response.message());
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new IOException("响应体为空");
                }

                try (okio.BufferedSource source = body.source()) {
                    StringBuilder messageBuilder = new StringBuilder();
                    okio.Buffer buffer = new okio.Buffer();

                    while (!source.exhausted()) {
                        source.read(buffer, 8192);

                        String chunk = buffer.readUtf8();
                        String[] lines = chunk.split("\\r?\\n");

                        for (String line : lines) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);

                                // 流式响应处理逻辑
                                if ("[DONE]".equals(data)) {
                                    continue;
                                }

                                try {
                                    // 简化处理，直接发送数据内容
                                    messageBuilder.append(data);
                                    onMessage.accept(data);
                                } catch (Exception e) {
                                    onError.accept(e);
                                    return;
                                }
                            }
                        }
                    }
                }

                onComplete.run();
            } catch (Exception e) {
                onError.accept(e);
            }
        }

        @Override
        public void close() throws IOException {
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
        }
    }
}