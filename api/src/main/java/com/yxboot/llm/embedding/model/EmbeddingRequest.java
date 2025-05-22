package com.yxboot.llm.embedding.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 嵌入请求类
 * 封装文本嵌入请求的参数
 * 
 * @author Boya
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest {

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 输入文本列表
     */
    @Builder.Default
    private List<String> input = new ArrayList<>();

    /**
     * 维度大小，可选
     * 某些模型支持自定义维度
     */
    private Integer dimensions;

    /**
     * 额外参数
     */
    private Map<String, Object> options;

    /**
     * 创建单文本嵌入请求
     * 
     * @param text 输入文本
     * @return 嵌入请求对象
     */
    public static EmbeddingRequest of(String text) {
        List<String> input = new ArrayList<>();
        input.add(text);

        return EmbeddingRequest.builder()
                .input(input)
                .build();
    }

    /**
     * 创建多文本嵌入请求
     * 
     * @param texts 输入文本列表
     * @return 嵌入请求对象
     */
    public static EmbeddingRequest of(List<String> texts) {
        return EmbeddingRequest.builder()
                .input(texts)
                .build();
    }

    /**
     * 创建带模型名称的嵌入请求
     * 
     * @param texts     输入文本列表
     * @param modelName 模型名称
     * @return 嵌入请求对象
     */
    public static EmbeddingRequest of(List<String> texts, String modelName) {
        return EmbeddingRequest.builder()
                .input(texts)
                .modelName(modelName)
                .build();
    }
}