package com.yxboot.llm.provider.zhipu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yxboot.llm.chat.ChatModel;
import com.yxboot.llm.chat.ChatResponse;
import com.yxboot.llm.chat.ChatResponse.TokenUsage;
import com.yxboot.llm.chat.ModelProvider;
import com.yxboot.llm.chat.message.AiMessage;
import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.prompt.ChatOptions;
import com.yxboot.llm.chat.prompt.Prompt;
import com.yxboot.llm.provider.zhipu.kernel.ZhipuAIApi;
import com.yxboot.llm.provider.zhipu.kernel.ZhipuAIMessage;
import com.yxboot.llm.provider.zhipu.kernel.ZhipuAIRequest;
import com.yxboot.llm.provider.zhipu.kernel.ZhipuAIResponse;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 智谱AI聊天模型实现
 * 
 * @author Boya
 */
@Slf4j
@Component
public class ZhipuAIChatModel implements ChatModel {

    /**
     * 默认配置
     */
    private ZhipuAIChatConfig config;

    /**
     * 智谱API客户端
     */
    private ZhipuAIApi zhipuApi;

    /**
     * 无参构造函数 - 供Spring使用
     */
    public ZhipuAIChatModel() {
        // Spring依赖注入需要此构造函数
    }

    /**
     * 构造函数，使用默认配置
     * 
     * @param zhipuApi 智谱API
     * @param apiKey   API密钥
     */
    public ZhipuAIChatModel(ZhipuAIApi zhipuApi, String apiKey) {
        this.zhipuApi = zhipuApi;
        this.config = ZhipuAIChatConfig.of(apiKey);
    }

    /**
     * 构造函数，使用自定义配置
     * 
     * @param zhipuApi 智谱API
     * @param config   智谱配置
     */
    public ZhipuAIChatModel(ZhipuAIApi zhipuApi, ZhipuAIChatConfig config) {
        this.zhipuApi = zhipuApi;
        this.config = config;
    }

    /**
     * 设置配置
     * 
     * @param config 智谱配置
     * @return 当前模型实例
     */
    public ZhipuAIChatModel withConfig(ZhipuAIChatConfig config) {
        this.config = config;
        return this;
    }

    /**
     * 设置API密钥
     * 
     * @param apiKey API密钥
     * @return 当前模型实例
     */
    public ZhipuAIChatModel withApiKey(String apiKey) {
        if (this.config != null) {
            this.config = this.config.withApiKey(apiKey);
        } else {
            this.config = ZhipuAIChatConfig.of(apiKey);
        }
        return this;
    }

    /**
     * 设置智谱客户端
     * 
     * @param zhipuApi 智谱API
     */
    @Autowired
    public void setZhipuApi(ZhipuAIApi zhipuApi) {
        this.zhipuApi = zhipuApi;
    }

    /**
     * 设置客户端
     * 
     * @param zhipuApi 智谱API
     * @return 当前模型实例
     */
    public ZhipuAIChatModel withClient(ZhipuAIApi zhipuApi) {
        this.zhipuApi = zhipuApi;
        return this;
    }

    /**
     * 获取API密钥
     * 
     * @return API密钥
     */
    public String getApiKey() {
        return config != null ? config.getApiKey() : null;
    }

    /**
     * 获取提供商信息
     */
    @Override
    public ModelProvider getProvider() {
        return ModelProvider.ZHIPU;
    }

    /**
     * 获取默认选项
     */
    @Override
    public ChatOptions getDefaultOptions() {
        return ChatOptions.builder()
                .model(config.getModel())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .stream(true)
                .build();
    }

    /**
     * 将Message对象转换为智谱消息
     */
    private List<ZhipuAIMessage> convertMessages(List<Message> messages) {
        List<ZhipuAIMessage> result = new ArrayList<>();

        for (Message message : messages) {
            String role;
            // 根据消息类型确定角色
            switch (message.type()) {
                case SYSTEM:
                    role = "system";
                    break;
                case USER:
                    role = "user";
                    break;
                case ASSISTANT:
                    role = "assistant";
                    break;
                case TOOL_EXECUTION_RESULT:
                    role = "tool"; // 智谱AI的工具结果消息类型
                    break;
                case CUSTOM:
                    role = "user"; // 自定义消息默认作为用户消息
                    break;
                default:
                    role = "user";
            }
            String content = message.getContent();
            // 添加到知启消息列表
            result.add(ZhipuAIMessage.builder().role(role).content(content).build());
        }

        return result;
    }

    /**
     * 构建智谱请求
     */
    private ZhipuAIRequest buildRequest(Prompt prompt, boolean stream) {
        // 获取选项
        ChatOptions options = extractChatOptions(prompt);

        // 使用配置值填充缺失选项
        String model = options.getModel() != null ? options.getModel() : config.getModel();
        Float temperature = options.getTemperature();
        Integer maxTokens = options.getMaxTokens();
        Float topP = options.getTopP();

        // 构建请求
        ZhipuAIRequest.ZhipuAIRequestBuilder builder = ZhipuAIRequest.builder()
                .model(model)
                .messages(convertMessages(prompt.getMessages()))
                .temperature(temperature != null ? temperature : config.getTemperature())
                .maxTokens(maxTokens != null ? maxTokens : config.getMaxTokens())
                .topP(topP != null ? topP : config.getTopP())
                .stream(stream);

        // 检查是否有系统消息参数
        if (options.getParameters().containsKey("system_prompt")) {
            String systemPrompt = (String) options.getParameters().get("system_prompt");
            builder.system(systemPrompt);
        }

        // 添加其他特殊参数
        if (options.getParameters().containsKey("top_k")) {
            builder.topK((Integer) options.getParameters().get("top_k"));
        }

        if (options.getParameters().containsKey("stop")) {
            @SuppressWarnings("unchecked")
            List<String> stopList = (List<String>) options.getParameters().get("stop");
            builder.stop(stopList);
        }

        if (options.getParameters().containsKey("tools")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> toolsList = (List<Map<String, Object>>) options.getParameters().get("tools");
            builder.tools(toolsList);
        }

        return builder.build();
    }

    /**
     * 从提示词中提取聊天选项
     */
    private ChatOptions extractChatOptions(Prompt prompt) {
        if (prompt.getOptions() != null) {
            return (ChatOptions) prompt.getOptions();
        }
        return getDefaultOptions();
    }

    /**
     * 流式调用方法实现
     */
    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // 构建智谱请求
        ZhipuAIRequest request = buildRequest(prompt, true);

        // 定义数据处理器
        ZhipuAIApi.StreamDataProcessor dataProcessor = (data, mapper) -> {
            ZhipuAIResponse response = mapper.readValue(data, ZhipuAIResponse.class);

            // 检查响应状态
            if (response.getCode() != null && response.getCode() != 0) {
                log.error("智谱API调用失败: {}", response.getMessage());
                throw new RuntimeException("智谱API调用失败: " + response.getMessage());
            }

            // 获取内容
            if (response.getChoices() != null && !response.getChoices().isEmpty() &&
                    response.getChoices().get(0).getDelta() != null) {
                String content = response.getChoices().get(0).getDelta().getContent();
                return content != null ? content : "";
            }

            return "";
        };

        // 发送流式请求获取字符流
        Flux<String> contentFlux = zhipuApi.sendStreamRequest(
                request,
                getApiKey(),
                config.getBaseUrl(),
                dataProcessor,
                error -> log.error("流式请求错误: {}", error.getMessage()));

        // 将字符流转换为ChatResponse流
        return contentFlux.map(chunk -> {
            // 创建AI消息
            AiMessage aiMessage = new AiMessage(chunk);

            // 构建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", request.getModel());
            metadata.put("streaming", true);

            // 构建并返回ChatResponse对象
            return ChatResponse.builder()
                    .content(chunk)
                    .message(aiMessage)
                    .metadata(metadata)
                    .build();
        });
    }

    /**
     * 发送同步请求（非流式）
     */
    private ChatResponse callSync(Prompt prompt) {
        // 构建智谱请求
        ZhipuAIRequest request = buildRequest(prompt, false);

        // 发送请求
        ZhipuAIResponse response = zhipuApi.sendRequest(
                request,
                getApiKey(),
                config.getBaseUrl(),
                ZhipuAIResponse.class);

        // 检查响应状态
        if (response.getCode() != null && response.getCode() != 0) {
            log.error("智谱API调用失败: {}", response.getMessage());
            throw new RuntimeException("智谱API调用失败: " + response.getMessage());
        }

        // 获取内容
        String content = "";
        if (response.getChoices() != null && !response.getChoices().isEmpty() &&
                response.getChoices().get(0).getMessage() != null) {
            content = response.getChoices().get(0).getMessage().getContent();
        }

        // 获取token使用情况
        TokenUsage tokenUsage = null;
        if (response.getUsage() != null) {
            tokenUsage = TokenUsage.of(
                    response.getUsage().getPromptTokens(),
                    response.getUsage().getCompletionTokens());
        }

        // 构建元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", response.getId());
        metadata.put("model", response.getModel());
        metadata.put("created", response.getCreated());

        // 构建AI消息
        AiMessage aiMessage = new AiMessage(content);

        // 构建响应
        return ChatResponse.builder()
                .content(content)
                .message(aiMessage)
                .tokenUsage(tokenUsage)
                .metadata(metadata)
                .build();
    }

    /**
     * 重写默认的call方法，优先使用非流式请求
     */
    @Override
    public ChatResponse call(Prompt prompt) {
        ChatOptions chatOptions = extractChatOptions(prompt);

        // 如果明确指定使用流式调用，则使用默认的流式处理
        if (chatOptions.getStream() != null && chatOptions.getStream()) {
            return streamToResponse(prompt).block();
        }

        // 否则使用同步调用
        return callSync(prompt);
    }

    /**
     * 初始化方法
     */
    @PostConstruct
    public void init() {
        if (config == null) {
            // 使用默认配置
            this.config = ZhipuAIChatConfig.defaultConfig();
        }
    }
}