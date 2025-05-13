package com.yxboot.modules.ai.provider;

import java.io.IOException;

import com.yxboot.modules.ai.dto.ModelRequestDTO;
import com.yxboot.modules.ai.dto.ModelResponseDTO;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;

/**
 * 模型提供商接口
 * 所有模型提供商必须实现此接口
 * 
 * @author Boya
 */
public interface ModelProvider {

    /**
     * 获取提供商名称
     * 
     * @return 提供商名称
     */
    String getProviderName();

    /**
     * 检查提供商是否支持该模型
     * 
     * @param provider 提供商信息
     * @param model    模型信息
     * @return 是否支持
     */
    boolean supports(Provider provider, Model model);

    /**
     * 聊天请求
     * 
     * @param provider 提供商信息
     * @param model    模型信息
     * @param request  请求参数
     * @return 响应结果
     * @throws IOException 请求异常
     */
    ModelResponseDTO chatCompletion(Provider provider, Model model, ModelRequestDTO request) throws IOException;

    /**
     * 流式聊天请求
     * 
     * @param provider 提供商信息
     * @param model    模型信息
     * @param request  请求参数
     * @return 流式响应处理器
     * @throws IOException 请求异常
     */
    SSEStreamHandler streamingChatCompletion(Provider provider, Model model, ModelRequestDTO request)
            throws IOException;
}