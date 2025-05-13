package com.yxboot.modules.ai.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yxboot.common.api.Result;
import com.yxboot.common.exception.ApiException;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.ai.dto.ModelRequestDTO;
import com.yxboot.modules.ai.dto.ModelResponseDTO;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.service.AiService;
import com.yxboot.modules.ai.service.ModelService;
import com.yxboot.modules.ai.service.ProviderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 模型调用控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI模型调用接口", description = "提供AI模型调用的相关接口")
public class AiController {

    private final AiService aiService;
    private final ModelService modelService;
    private final ProviderService providerService;

    /**
     * 创建聊天完成
     * 
     * @param request      请求参数
     * @param securityUser 当前用户
     * @return 响应结果
     * @throws IOException 请求异常
     */
    @PostMapping("/chat")
    @Operation(summary = "创建聊天完成", description = "使用指定的模型创建聊天完成")
    public Result<ModelResponseDTO> createChatCompletion(
            @RequestBody ModelRequestDTO request,
            @AuthenticationPrincipal SecurityUser securityUser) throws IOException {

        // 验证必要参数
        if (request.getModelId() == null) {
            throw new ApiException("模型ID不能为空");
        }

        // 获取模型和提供商
        Model model = modelService.getById(request.getModelId());
        if (model == null) {
            throw new ApiException("模型不存在");
        }
        Provider provider = providerService.getById(model.getProviderId());
        if (provider == null) {
            throw new ApiException("模型提供商不存在");
        }

        ModelResponseDTO response = aiService.chatCompletion(provider, model, request);
        return Result.success("模型调用成功", response);
    }

    /**
     * 创建流式聊天完成
     * 
     * @param request      请求参数
     * @param securityUser 当前用户
     * @return 流式响应
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "创建流式聊天完成", description = "使用指定的模型创建流式聊天完成，使用SSE进行响应")
    public SseEmitter createStreamingChatCompletion(
            @RequestBody ModelRequestDTO request,
            @AuthenticationPrincipal SecurityUser securityUser) {

        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 在单独的线程中处理流式响应
        new Thread(() -> {
            try {
                // 验证必要参数
                if (request.getModelId() == null) {
                    throw new ApiException("模型ID不能为空");
                }

                // 获取模型和提供商
                Model model = modelService.getById(request.getModelId());
                if (model == null) {
                    throw new ApiException("模型不存在");
                }
                Provider provider = providerService.getById(model.getProviderId());
                if (provider == null) {
                    throw new ApiException("模型提供商不存在");
                }

                // 设置流式请求标志
                request.setStream(true);

                aiService.streamingChatCompletion(provider, model, request, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}