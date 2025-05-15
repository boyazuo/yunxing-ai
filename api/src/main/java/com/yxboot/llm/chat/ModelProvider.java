package com.yxboot.llm.chat;

/**
 * 模型提供商接口
 * 用于标识不同的大模型服务提供商
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
     * 创建一个模型提供商实例
     * 
     * @param name 提供商名称
     * @return 模型提供商
     */
    static ModelProvider of(String name) {
        return () -> name;
    }

    /**
     * 预定义的提供商常量
     */
    ModelProvider ZHIPU = of("ZhipuAI");
    ModelProvider QIANWEN = of("QianWen");
    ModelProvider OPENAI = of("OpenAI");
    ModelProvider LOCAL = of("Local");
    ModelProvider OTHER = of("Other");
}
