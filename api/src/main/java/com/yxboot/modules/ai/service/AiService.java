package com.yxboot.modules.ai.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yxboot.llm.chat.ChatModel;
import com.yxboot.llm.chat.ChatResponse;
import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.message.SystemMessage;
import com.yxboot.llm.chat.message.UserMessage;
import com.yxboot.llm.chat.prompt.ChatOptions;
import com.yxboot.llm.chat.prompt.Prompt;
import com.yxboot.modules.ai.dto.ModelRequestDTO;
import com.yxboot.modules.ai.dto.ModelResponseDTO;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.enums.MessageStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 模型调用服务
 * 负责根据Provider选择正确的ChatModel实现，调用大模型，并处理响应结果
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatModelFactory chatModelFactory;
    private final MessageService messageService;

    /**
     * 创建聊天完成
     * 根据提供商动态选择对应的ChatModel实现处理请求
     * Model仅作为配置参数传入，不影响ChatModel的选择
     * 参数验证已在控制器层进行
     * 
     * @param provider 提供商信息，用于选择正确的ChatModel实现
     * @param request  请求参数
     * @return 响应结果
     * @throws IOException 请求异常
     */
    public ModelResponseDTO chatCompletion(Provider provider, ModelRequestDTO request) throws IOException {
        log.info("开始处理聊天请求，提供商：{}，模型：{}", provider.getProviderName(), request.getModelName());

        try {
            // 获取对应的ChatModel实现
            ChatModel chatModel = chatModelFactory.createChatModel(provider);

            // 构建提示词
            Prompt prompt = buildPrompt(request);

            // 调用模型
            ChatResponse response = chatModel.call(prompt);

            // 转换为DTO返回
            return buildResponseDTO(response, provider, request.getModelName());
        } catch (Exception e) {
            log.error("聊天请求处理异常", e);
            throw new IOException("模型调用失败：" + e.getMessage(), e);
        }
    }

    /**
     * 创建流式聊天完成
     * 根据提供商动态选择对应的ChatModel实现处理流式请求
     * Model仅作为配置参数传入，不影响ChatModel的选择
     * 参数验证已在控制器层进行
     * 
     * @param provider  提供商信息，用于选择正确的ChatModel实现
     * @param model     模型信息，作为配置参数
     * @param request   请求参数
     * @param emitter   SSE发射器
     * @param messageId 消息ID，用于在流处理完成时更新消息状态
     * @throws IOException 请求异常
     */
    public void streamingChatCompletion(Provider provider, ModelRequestDTO request,
            SseEmitter emitter, Long messageId) throws IOException {
        try {
            // 获取对应的ChatModel实现（仅根据Provider选择）
            ChatModel chatModel = chatModelFactory.createChatModel(provider);

            // 构建提示词
            Prompt prompt = buildPrompt(request);

            // 用于收集完整响应的StringBuilder
            StringBuilder fullResponseBuilder = new StringBuilder();

            // 发送初始元数据
            emitter.send(SseEmitter.event()
                    .name("metadata")
                    .data(Map.of(
                            "conversationId", request.getConversationId(),
                            "messageId", messageId)));

            // 调用流式接口
            Flux<ChatResponse> responseStream = chatModel.stream(prompt);

            // 处理流式响应
            responseStream
                    .doOnNext(response -> {
                        try {
                            // 获取响应内容
                            String chunk = response.getContent();

                            // 收集完整响应
                            fullResponseBuilder.append(chunk);

                            // 确保chunk不为空
                            if (chunk != null && !chunk.trim().isEmpty()) {
                                // 发送数据块，格式化为SSE格式
                                emitter.send(SseEmitter.event().data(Map.of("chunk", chunk)));
                            }
                        } catch (IOException e) {
                            log.error("发送流式数据异常", e);
                        }
                    })
                    .doOnComplete(() -> {
                        try {
                            // 流结束，更新消息内容和状态
                            if (messageId != null) {
                                // 使用注入的MessageService更新消息
                                messageService.updateMessageAnswer(
                                        messageId,
                                        fullResponseBuilder.toString(),
                                        MessageStatus.COMPLETED);
                            }

                            // 发送结束事件
                            emitter.send(SseEmitter.event()
                                    .name("end")
                                    .data(""));

                            // 流结束
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("关闭SSE发射器或更新消息状态异常", e);
                        }
                    })
                    .doOnError(error -> {
                        try {
                            // 错误处理，更新消息状态为失败
                            if (messageId != null) {
                                messageService.updateMessageAnswer(
                                        messageId,
                                        "处理失败: " + error.getMessage(),
                                        MessageStatus.FAILED);
                            }
                            // 错误处理
                            log.error("流式处理发生错误", error);
                            emitter.completeWithError(error);
                        } catch (Exception e) {
                            log.error("关闭SSE发射器异常", e);
                        }
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("流式聊天请求处理异常", e);
            // 更新消息状态为失败
            if (messageId != null) {
                messageService.updateMessageAnswer(
                        messageId,
                        "处理失败: " + e.getMessage(),
                        MessageStatus.FAILED);
            }
            emitter.completeWithError(e);
        }
    }

    /**
     * 构建提示词对象
     * 
     * @param model   模型信息
     * @param request 请求参数
     * @return 提示词对象
     */
    private Prompt buildPrompt(ModelRequestDTO request) {
        // 转换消息
        List<Message> messages = request.getMessages();

        // 添加系统提示词
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(0, new SystemMessage(request.getSystemPrompt()));
        }

        // 添加用户消息
        messages.add(new UserMessage(request.getPrompt()));

        // 构建选项参数映射
        ChatOptions options = new ChatOptions();
        options.setModel(request.getModelName());
        options.setStream(request.getStream());
        if (request.getMaxTokens() != null) {
            options.setMaxTokens(request.getMaxTokens());
        }

        if (request.getTemperature() != null) {
            options.setTemperature(request.getTemperature());
        }

        if (request.getTopP() != null) {
            options.setTopP(request.getTopP());
        }
        return new Prompt(messages, options);
    }

    /**
     * 构建响应DTO
     * 
     * @param response ChatResponse对象
     * @param provider 提供商信息
     * @param model    模型信息
     * @return 响应DTO
     */
    private ModelResponseDTO buildResponseDTO(ChatResponse response, Provider provider, String modelName) {
        // 从响应中提取token使用情况
        int promptTokens = 0;
        int completionTokens = 0;
        int totalTokens = 0;

        // 如果存在token使用统计信息
        if (response.getTokenUsage() != null) {
            promptTokens = response.getTokenUsage().getInputTokens();
            completionTokens = response.getTokenUsage().getOutputTokens();
            totalTokens = response.getTokenUsage().getTotalTokens();
        }

        return ModelResponseDTO.builder()
                .content(response.getContent())
                .model(modelName)
                .provider(provider.getProviderName())
                .createTime(LocalDateTime.now())
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .build();
    }
}