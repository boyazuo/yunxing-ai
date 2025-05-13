package com.yxboot.modules.ai.provider.zhipu;

import org.springframework.context.annotation.Configuration;

/**
 * 智谱AI配置类
 * 
 * @author Boya
 */
@Configuration
public class ZhipuConfig {
    /**
     * 默认智谱AI接口地址
     */
    public static final String DEFAULT_ZHIPU_HOST = "https://open.bigmodel.cn";

    /**
     * API路径
     */
    public static final String API_PATH = "/api/paas/v4/chat/completions";

    /**
     * 默认超时时间（秒）
     */
    public static final long DEFAULT_TIMEOUT_SECONDS = 60;

    /**
     * 默认缓冲区大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;
}