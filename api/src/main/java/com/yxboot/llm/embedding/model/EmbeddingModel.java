package com.yxboot.llm.embedding.model;

import java.util.List;

import com.yxboot.llm.embedding.config.EmbeddingConfig;

/**
 * 嵌入模型接口，用于将文本转换为向量表示
 */
public interface EmbeddingModel {

    /**
     * 将单个文本编码为向量
     *
     * @param text 输入文本
     * @return 向量表示（浮点数组）
     */
    float[] embed(String text);

    /**
     * 批量将多个文本编码为向量
     *
     * @param texts 输入文本列表
     * @return 向量表示列表
     */
    List<float[]> embedAll(List<String> texts);

    /**
     * 处理嵌入请求并返回嵌入响应
     *
     * @param request 嵌入请求对象
     * @return 嵌入响应对象
     */
    EmbeddingResponse embedRequest(EmbeddingRequest request);

    /**
     * 配置模型
     *
     * @param config 配置信息
     */
    void configure(EmbeddingConfig config);

    /**
     * 设置API密钥
     *
     * @param apiKey API密钥
     * @return 当前模型实例，支持链式调用
     */
    EmbeddingModel withApiKey(String apiKey);

    /**
     * 获取模型名称
     *
     * @return 模型名称
     */
    String getModelName();

    /**
     * 获取向量维度
     *
     * @return 向量维度
     */
    int getEmbeddingDimension();
}