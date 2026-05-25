package com.yxboot.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.yxboot.ai.config.AiProperties;
import com.yxboot.ai.vector.AiQueryResult;
import com.yxboot.modules.app.entity.AppConfig;

@ExtendWith(MockitoExtension.class)
class RagChatPromptServiceTest {

    @Mock
    private AiVectorRetrieverService vectorRetrieverService;

    @InjectMocks
    private RagChatPromptService ragChatPromptService;

    private AiProperties aiProperties;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        ragChatPromptService = new RagChatPromptService(vectorRetrieverService, aiProperties);
    }

    @Test
    void build_withoutDatasets_usesOriginalPrompt() {
        AppConfig appConfig = new AppConfig();
        appConfig.setSysPrompt("你是助手");

        var result = ragChatPromptService.build("你好", appConfig);

        assertThat(result.isRagMode()).isFalse();
        assertThat(result.getUserPrompt()).isEqualTo("你好");
        assertThat(result.getSystemPrompt()).isEqualTo("你是助手");
        assertThat(result.getDirectResponse()).isNull();
    }

    @Test
    void build_strictModeWithoutResults_returnsDirectResponse() {
        AppConfig appConfig = appConfigWithDataset(1L);
        when(vectorRetrieverService.retrieve(anyLong(), anyString(), anyInt(), anyFloat()))
                .thenReturn(List.of());

        var result = ragChatPromptService.build("云行 AI 是什么？", appConfig);

        assertThat(result.isRagMode()).isTrue();
        assertThat(result.isHasRetrievedContext()).isFalse();
        assertThat(result.getDirectResponse()).contains("未找到");
    }

    @Test
    void build_strictModeWithResults_buildsGroundedPrompt() {
        AppConfig appConfig = appConfigWithDataset(1L);
        when(vectorRetrieverService.retrieve(anyLong(), anyString(), anyInt(), anyFloat()))
                .thenReturn(List.of(AiQueryResult.builder().text("云行 AI 是企业知识库平台").score(0.92f).build()));

        var result = ragChatPromptService.build("云行 AI 是什么？", appConfig);

        assertThat(result.isRagMode()).isTrue();
        assertThat(result.isHasRetrievedContext()).isTrue();
        assertThat(result.getDirectResponse()).isNull();
        assertThat(result.getUserPrompt()).contains("【参考内容】").contains("云行 AI 是企业知识库平台");
        assertThat(result.getUserPrompt()).contains("不得使用外部知识");
        assertThat(result.getUserPrompt()).contains("Markdown");
        assertThat(result.getSystemPrompt()).contains("严格的知识库问答助手");
        assertThat(result.getSystemPrompt()).contains("回复组织");
    }

    @Test
    void build_nonStrictModeWithoutResults_keepsOriginalPrompt() {
        aiProperties.getRag().setStrictMode(false);
        AppConfig appConfig = appConfigWithDataset(1L);
        when(vectorRetrieverService.retrieve(anyLong(), anyString(), anyInt(), anyFloat()))
                .thenReturn(List.of());

        var result = ragChatPromptService.build("随便聊聊", appConfig);

        assertThat(result.getDirectResponse()).isNull();
        assertThat(result.getUserPrompt()).isEqualTo("随便聊聊");
    }

    private AppConfig appConfigWithDataset(Long datasetId) {
        AppConfig appConfig = new AppConfig();
        appConfig.setDatasets("[{\"datasetId\":" + datasetId + ",\"isActive\":true}]");
        appConfig.setSysPrompt("请用中文回答");
        return appConfig;
    }
}
