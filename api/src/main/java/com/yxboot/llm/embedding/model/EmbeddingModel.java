package com.yxboot.llm.embedding.model;

import java.util.List;

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