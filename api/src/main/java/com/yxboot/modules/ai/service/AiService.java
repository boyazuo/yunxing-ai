package com.yxboot.modules.ai.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yxboot.common.exception.ApiException;
import com.yxboot.modules.ai.dto.ModelRequestDTO;
import com.yxboot.modules.ai.dto.ModelResponseDTO;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.provider.ModelProvider;
import com.yxboot.modules.ai.provider.SSEStreamHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型调用服务
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    // 注入所有模型提供商策略
    private final List<ModelProvider> providers;

    /**
     * 创建聊天完成
     * 根据提供商和模型动态选择对应的提供商处理请求
     * 参数验证已在控制器层进行
     * 
     * @param provider 提供商信息
     * @param model    模型信息
     * @param request  请求参数
     * @return 响应结果
     * @throws IOException 请求异常
     */
    public ModelResponseDTO chatCompletion(Provider provider, Model model, ModelRequestDTO request)
            throws IOException {
        // 根据提供商和模型查找合适的策略
        ModelProvider modelProvider = findSupportedProvider(provider, model);

        try {
            return modelProvider.chatCompletion(provider, model, request);
        } catch (Exception e) {
            log.error("调用模型异常: {}", e.getMessage(), e);
            return ModelResponseDTO.builder()
                    .errorMessage("调用模型异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 创建流式聊天完成
     * 处理SSE流式响应
     * 参数验证已在控制器层进行
     * 
     * @param provider 提供商信息
     * @param model    模型信息
     * @param request  请求参数
     * @param emitter  SSE发射器
     * @throws IOException 请求异常
     */
    public void streamingChatCompletion(Provider provider, Model model, ModelRequestDTO request,
            SseEmitter emitter) throws IOException {
        // 根据提供商和模型查找合适的策略
        ModelProvider modelProvider = findSupportedProvider(provider, model);

        try {
            // 创建流式处理器并处理流
            SSEStreamHandler streamHandler = modelProvider.streamingChatCompletion(provider, model, request);
            streamHandler.handle(emitter);
        } catch (Exception e) {
            log.error("创建流式响应异常: {}", e.getMessage(), e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 查找支持指定提供商和模型的策略
     * 
     * @param provider 提供商信息
     * @param model    模型信息
     * @return 模型提供商策略
     * @throws ApiException 如果找不到支持的策略
     */
    public ModelProvider findSupportedProvider(Provider provider, Model model) {
        return providers.stream()
                .filter(p -> p.supports(provider, model))
                .findFirst()
                .orElseThrow(() -> new ApiException("不支持的模型提供商: " + provider.getProviderName()));
    }
}