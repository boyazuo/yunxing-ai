package com.yxboot.modules.ai.provider.zhipu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxboot.modules.ai.dto.ChatMessageDTO;
import com.yxboot.modules.ai.dto.ModelRequestDTO;
import com.yxboot.modules.ai.dto.ModelResponseDTO;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.provider.ModelProvider;
import com.yxboot.modules.ai.provider.SSEStreamHandler;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 智谱AI模型提供商实现
 * 
 * @author Boya
 */
@Slf4j
@Component
public class ZhipuAIProvider implements ModelProvider {

    private static final String DEFAULT_ZHIPU_HOST = "https://open.bigmodel.cn";
    private static final String API_PATH = "/api/paas/v4/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final long DEFAULT_TIMEOUT = 60;

    private final OkHttpClient httpClient;

    public ZhipuAIProvider() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getProviderName() {
        return "ZhipuAI";
    }

    @Override
    public boolean supports(Provider provider, Model model) {
        return "ZhipuAI".equalsIgnoreCase(provider.getProviderName());
    }

    @Override
    public ModelResponseDTO chatCompletion(Provider provider, Model model, ModelRequestDTO request)
            throws IOException {
        String apiKey = provider.getApiKey();
        String baseUrl = provider.getEndpoint();

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = DEFAULT_ZHIPU_HOST;
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
            log.debug("智谱AI响应: {}", responseBody);

            // 解析JSON响应
            ZhipuResponse zhipuResponse = parseResponse(responseBody);

            return ModelResponseDTO.builder()
                    .content(zhipuResponse.getContent())
                    .model(model.getModelName())
                    .requestId(zhipuResponse.getRequestId())
                    .finishReason(zhipuResponse.getFinishReason())
                    .promptTokens(zhipuResponse.getPromptTokens())
                    .completionTokens(zhipuResponse.getCompletionTokens())
                    .totalTokens(zhipuResponse.getTotalTokens())
                    .build();
        }
    }

    @Override
    public SSEStreamHandler streamingChatCompletion(Provider provider, Model model, ModelRequestDTO request)
            throws IOException {
        String apiKey = provider.getApiKey();
        String baseUrl = provider.getEndpoint();

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = DEFAULT_ZHIPU_HOST;
        }

        // 设置流式请求参数
        request.setStream(true);
        String requestJson = buildRequestJson(model, request);

        Request httpRequest = new Request.Builder()
                .url(baseUrl + API_PATH)
                .header("Authorization", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .post(RequestBody.create(requestJson, JSON))
                .build();

        return new ZhipuStreamHandler(httpClient, httpRequest);
    }

    private String buildRequestJson(Model model, ModelRequestDTO request) {
        List<ZhipuMessage> messages = new ArrayList<>();

        // 添加系统消息
        if (request.getSystemMessage() != null &&
                !request.getSystemMessage().isEmpty()) {
            messages.add(new ZhipuMessage("system", request.getSystemMessage()));
        }

        // 添加聊天历史
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (ChatMessageDTO msg : request.getMessages()) {
                messages.add(new ZhipuMessage(msg.getRole(), msg.getContent()));
            }
        } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
            // 如果没有消息但有提示，将提示作为用户消息
            messages.add(new ZhipuMessage("user", request.getPrompt()));
        }

        // 构建请求JSON
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\":\"").append(model.getModelName()).append("\",");
        json.append("\"messages\":[");

        for (int i = 0; i < messages.size(); i++) {
            ZhipuMessage msg = messages.get(i);
            json.append("{\"role\":\"").append(msg.getRole()).append("\",");
            json.append("\"content\":\"").append(msg.getContent()).append("\"}");
            if (i < messages.size() - 1) {
                json.append(",");
            }
        }

        json.append("],");
        json.append("\"temperature\":").append(request.getTemperature()).append(",");
        if (request.getMaxTokens() != null) {
            json.append("\"max_tokens\":").append(request.getMaxTokens()).append(",");
        } else if (model.getMaxTokens() != null) {
            json.append("\"max_tokens\":").append(model.getMaxTokens()).append(",");
        }
        json.append("\"top_p\":").append(request.getTopP()).append(",");
        json.append("\"stream\":").append(request.getStream());
        json.append("}");

        return json.toString();
    }

    private ZhipuResponse parseResponse(String responseBody) {
        // 使用Jackson进行JSON解析
        ZhipuResponse response = ZhipuResponse.builder().build();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // 提取请求ID
            if (rootNode.has("id")) {
                response.setRequestId(rootNode.get("id").asText());
            } else if (rootNode.has("request_id")) {
                response.setRequestId(rootNode.get("request_id").asText());
            }

            // 解析内容
            if (rootNode.has("choices") && rootNode.get("choices").isArray() && rootNode.get("choices").size() > 0) {
                JsonNode choiceNode = rootNode.get("choices").get(0);
                if (choiceNode.has("message") && choiceNode.get("message").has("content")) {
                    response.setContent(choiceNode.get("message").get("content").asText());
                }

                // 获取完成原因
                if (choiceNode.has("finish_reason")) {
                    response.setFinishReason(choiceNode.get("finish_reason").asText());
                }
            }

            // 解析用量统计
            if (rootNode.has("usage")) {
                JsonNode usageNode = rootNode.get("usage");
                if (usageNode.has("prompt_tokens")) {
                    response.setPromptTokens(usageNode.get("prompt_tokens").asInt());
                }
                if (usageNode.has("completion_tokens")) {
                    response.setCompletionTokens(usageNode.get("completion_tokens").asInt());
                }
                if (usageNode.has("total_tokens")) {
                    response.setTotalTokens(usageNode.get("total_tokens").asInt());
                }
            }

            // 模型名称
            if (rootNode.has("model")) {
                response.setModel(rootNode.get("model").asText());
            }

            // 创建时间
            if (rootNode.has("created")) {
                response.setCreated(rootNode.get("created").asLong());
            }

        } catch (Exception e) {
            log.error("解析智谱AI响应失败", e);
            response.setContent("解析响应失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 智谱AI消息
     */
    @Data
    @Builder
    private static class ZhipuMessage {
        private String role;
        private String content;
    }

    /**
     * 智谱AI响应
     */
    @Data
    @Builder
    private static class ZhipuResponse {
        private String content;
        private String requestId;
        private String model;
        private String finishReason;
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
        private Long created;
    }

    /**
     * 智谱AI流处理器
     */
    private static class ZhipuStreamHandler implements SSEStreamHandler {
        private final OkHttpClient client;
        private final Request request;
        private okhttp3.Call call;
        private static final ObjectMapper objectMapper = new ObjectMapper();

        public ZhipuStreamHandler(OkHttpClient client, Request request) {
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

                                if ("[DONE]".equals(data)) {
                                    continue;
                                }

                                try {
                                    // 解析JSON数据
                                    JsonNode jsonNode = objectMapper.readTree(data);
                                    String deltaContent = extractDeltaContent(jsonNode);

                                    if (deltaContent != null && !deltaContent.isEmpty()) {
                                        messageBuilder.append(deltaContent);
                                        onMessage.accept(deltaContent);
                                    }
                                } catch (Exception e) {
                                    log.error("解析流式响应失败", e);
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

        private String extractDeltaContent(JsonNode jsonNode) {
            try {
                if (jsonNode.has("choices") && jsonNode.get("choices").isArray()
                        && jsonNode.get("choices").size() > 0) {
                    JsonNode choiceNode = jsonNode.get("choices").get(0);
                    if (choiceNode.has("delta") && choiceNode.get("delta").has("content")) {
                        return choiceNode.get("delta").get("content").asText();
                    }
                }
                return "";
            } catch (Exception e) {
                log.error("提取增量内容失败", e);
                return "";
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