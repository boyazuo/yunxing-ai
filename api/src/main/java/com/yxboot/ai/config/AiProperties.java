package com.yxboot.ai.config;

import com.yxboot.modules.dataset.enums.SegmentMethod;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "yxboot.ai")
public class AiProperties {

    private ChatConfig chat = new ChatConfig();
    private EmbeddingConfig embedding = new EmbeddingConfig();
    private RetrieverConfig retriever = new RetrieverConfig();
    private RagConfig rag = new RagConfig();
    private DocumentConfig document = new DocumentConfig();

    @Data
    public static class ChatConfig {
        private String provider = "zhipuai";
        private String apiKey;
        private String model = "glm-4-flash";
        private Double temperature = 0.7;
        private Double topP = 0.95;
        private Integer maxTokens = 4096;
    }

    @Data
    public static class EmbeddingConfig {
        private String provider = "zhipuai";
        private String apiKey;
        private String model = "embedding-3";
        private Integer dimensions = 2048;
        /** Ollama 专用：服务地址，默认 http://localhost:11434 */
        private String baseUrl = "http://localhost:11434";
        /** HTTP 读超时（秒），本地大模型向量化较慢时需适当调大 */
        private Integer readTimeoutSeconds = 300;
        /** 单次 embedding 请求的分段数量，避免大批量请求超时 */
        private Integer batchSize = 16;

        /** 返回向量模型标识，格式 provider:model */
        public String toModelKey() {
            return provider + ":" + model;
        }
    }

    /** 当前系统配置的向量模型标识 */
    public String getEmbeddingModelKey() {
        return embedding.toModelKey();
    }

    @Data
    public static class RetrieverConfig {
        private int defaultLimit = 10;
        private float defaultMinScore = 0.0f;
    }

    @Data
    public static class RagConfig {
        /** 启用知识库后是否严格约束 AI 仅基于检索内容回答 */
        private boolean strictMode = true;
        /** 未检索到相关内容时的固定回复（跳过 LLM 调用） */
        private String noContextResponse = "抱歉，知识库中未找到与您问题相关的信息，我无法基于现有资料回答该问题。";
        /** 严格模式下的系统提示词 */
        private String systemPrompt = """
                你是一个严格的知识库问答助手。你的职责是根据用户消息中提供的参考内容准确回答用户问题。

                必须遵守以下规则：
                1. 仅使用参考内容中的信息作答，不得使用外部知识、常识或自行推测
                2. 若参考内容不足以完整回答问题，或与用户问题无关，必须明确回复：「抱歉，知识库中未找到与您问题相关的信息。」
                3. 不得编造事实、数据、名称、日期或细节
                4. 回答应简洁准确，不要提及「参考内容」「知识库」「检索」等内部术语
                5. 不要尝试回答参考内容未涵盖的问题
                """;
        /** 每个知识库检索条数 */
        private int perDatasetLimit = 5;
        /** 最终合并后的参考内容条数上限 */
        private int limit = 10;
        /** 检索最低相似度阈值 */
        private float minScore = 0.5f;
    }

    @Data
    public static class DocumentConfig {
        /** 分段方式：paragraph | chapter | parent_child */
        private String segmentMethod = "parent_child";
        /** 子块/普通块最大长度（字符数） */
        private Integer maxSegmentLength = 300;
        /** 块重叠长度（字符数） */
        private Integer overlapLength = 50;
        /** 父块大小（字符数，仅 parent_child 模式有效） */
        private Integer parentChunkSize = 1200;

        public SegmentMethod resolveSegmentMethod() {
            for (SegmentMethod method : SegmentMethod.values()) {
                if (method.getValue().equalsIgnoreCase(segmentMethod)) {
                    return method;
                }
            }
            return SegmentMethod.PARENT_CHILD;
        }
    }
}
