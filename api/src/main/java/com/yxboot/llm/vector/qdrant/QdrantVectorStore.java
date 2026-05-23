package com.yxboot.llm.vector.qdrant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.vector.AbstractVectorStore;
import com.yxboot.llm.vector.query.QueryResult;
import com.yxboot.llm.vector.query.VectorQuery;
import com.yxboot.util.HttpClient;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * QDrant向量存储实现
 * 
 * <p>
 * 该实现使用命名向量配置，确保：
 * </p>
 * <ul>
 * <li>创建集合时明确声明向量名称和维度</li>
 * <li>插入和搜索时使用一致的向量名称</li>
 * <li>支持自定义向量名称配置</li>
 * <li>验证集合配置的一致性</li>
 * <li>使用 Hutool JSONUtil 替换 ObjectMapper</li>
 * <li>使用项目封装的 HttpClient 替换 RestTemplate</li>
 * <li>使用 Builder 模式创建，不参与 IoC 管理</li>
 * </ul>
 * 
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * QdrantVectorStore vectorStore = QdrantVectorStore.builder()
 *         .config(qdrantConfig)
 *         .embeddingModel(embeddingModel)
 *         .autoCreateCollection(true)
 *         .build();
 * </pre>
 */
@Slf4j
@SuppressWarnings("unchecked")
public final class QdrantVectorStore extends AbstractVectorStore {

    private final QdrantConfig config;
    private final boolean autoCreateCollection;

    /**
     * 私有构造函数，只能通过 Builder 创建
     * 
     * @param builder 构建器
     */
    private QdrantVectorStore(Builder builder) {
        super(builder.config.getDefaultCollectionName(), builder.embeddingModel);
        this.config = builder.config;
        this.autoCreateCollection = builder.autoCreateCollection;

        // 验证配置
        validateConfig();

        // 如果启用自动创建集合
        if (autoCreateCollection) {
            // 确保默认集合存在
            if (!collectionExists(config.getDefaultCollectionName())) {
                createCollection(config.getDefaultCollectionName(), embeddingModel.getEmbeddingDimension());
            } else {
                // 验证现有集合的向量配置是否匹配
                validateCollectionVectorConfig(config.getDefaultCollectionName());
            }
        }
    }

    /**
     * 创建 Builder 实例
     * 
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类
     */
    public static class Builder {
        private QdrantConfig config;
        private EmbeddingModel embeddingModel;
        private boolean autoCreateCollection = true;

        private Builder() {}

        /**
         * 设置 QDrant 配置
         * 
         * @param config QDrant 配置
         * @return Builder 实例
         */
        public Builder config(QdrantConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置嵌入模型
         * 
         * @param embeddingModel 嵌入模型
         * @return Builder 实例
         */
        public Builder embeddingModel(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
            return this;
        }

        /**
         * 设置是否自动创建集合
         * 
         * @param autoCreateCollection 是否自动创建集合，默认为 true
         * @return Builder 实例
         */
        public Builder autoCreateCollection(boolean autoCreateCollection) {
            this.autoCreateCollection = autoCreateCollection;
            return this;
        }

        /**
         * 便捷方法：设置 QDrant 连接信息
         * 
         * @param host QDrant 服务器地址
         * @param port QDrant 服务器端口
         * @return Builder 实例
         */
        public Builder connection(String host, int port) {
            if (this.config != null) {
                this.config = this.config.toBuilder().host(host).port(port).build();
            } else {
                this.config = QdrantConfig.builder().host(host).port(port).build();
            }
            return this;
        }

        /**
         * 便捷方法：设置 API 密钥
         * 
         * @param apiKey API 密钥
         * @return Builder 实例
         */
        public Builder apiKey(String apiKey) {
            if (this.config != null) {
                this.config = this.config.toBuilder().apiKey(apiKey).build();
            } else {
                this.config = QdrantConfig.builder().apiKey(apiKey).build();
            }
            return this;
        }

        /**
         * 便捷方法：设置默认集合名称
         * 
         * @param collectionName 默认集合名称
         * @return Builder 实例
         */
        public Builder defaultCollection(String collectionName) {
            if (this.config != null) {
                this.config = this.config.toBuilder().defaultCollectionName(collectionName).build();
            } else {
                this.config = QdrantConfig.builder().defaultCollectionName(collectionName).build();
            }
            return this;
        }

        /**
         * 便捷方法：设置向量名称
         * 
         * @param vectorName 向量名称
         * @return Builder 实例
         */
        public Builder vectorName(String vectorName) {
            if (this.config != null) {
                this.config = this.config.toBuilder().vectorName(vectorName).build();
            } else {
                this.config = QdrantConfig.builder().vectorName(vectorName).build();
            }
            return this;
        }

        /**
         * 构建 QdrantVectorStore 实例
         * 
         * @return QdrantVectorStore 实例
         */
        public QdrantVectorStore build() {
            // 验证必要参数
            if (config == null) {
                throw new IllegalArgumentException("QdrantConfig 不能为空");
            }
            if (embeddingModel == null) {
                throw new IllegalArgumentException("EmbeddingModel 不能为空");
            }

            return new QdrantVectorStore(this);
        }
    }

    /**
     * 获取配置信息
     * 
     * @return QDrant 配置
     */
    public QdrantConfig getConfig() {
        return config;
    }

    /**
     * 是否启用自动创建集合
     * 
     * @return 是否自动创建集合
     */
    public boolean isAutoCreateCollection() {
        return autoCreateCollection;
    }

    /**
     * 添加单个向量
     */
    @Override
    public boolean addVector(String collectionName, String id, float[] vector, Map<String, Object> metadata,
            String text) {
        return addVectors(collectionName, Collections.singletonList(id), Collections.singletonList(vector),
                Collections.singletonList(metadata),
                Collections.singletonList(text)) == 1;
    }

    /**
     * 批量添加向量
     */
    @Override
    public int addVectors(String collectionName, List<String> ids, List<float[]> vectors,
            List<Map<String, Object>> metadataList,
            List<String> texts) {
        try {
            // 确保集合存在
            if (!ensureCollection(collectionName, embeddingModel.getEmbeddingDimension())) {
                log.error("确保集合存在失败: {}", collectionName);
                return 0;
            }

            // 构建请求URL
            String url = String.format("%s/collections/%s/points", config.getHttpUrl(), collectionName);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();

            // 准备点位列表
            List<Map<String, Object>> points = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                Map<String, Object> point = new HashMap<>();
                // 使用ID，如果为null则生成UUID
                point.put("id", ids.get(i) != null ? ids.get(i) : UUID.randomUUID().toString());

                // 使用命名向量格式
                Map<String, Object> vectorData = new HashMap<>();
                vectorData.put(config.getVectorName(), vectors.get(i));
                point.put("vector", vectorData);

                // 设置payload（元数据+文本）
                Map<String, Object> payload = new HashMap<>();
                if (metadataList != null && i < metadataList.size() && metadataList.get(i) != null) {
                    payload.putAll(metadataList.get(i));
                }

                // 将文本添加到payload
                payload.put("text", texts.get(i));

                point.put("payload", payload);
                points.add(point);
            }

            requestBody.put("points", points);

            // 发送请求
            Map<String, String> headers = createHeaders();
            String response = HttpClient.putJson(url, JSONUtil.toJsonStr(requestBody), headers);

            // 简单检查响应不为空即认为成功
            if (StrUtil.isNotBlank(response)) {
                return ids.size();
            } else {
                log.error("添加向量失败: 响应为空");
                return 0;
            }
        } catch (Exception e) {
            log.error("添加向量失败", e);
            return 0;
        }
    }

    /**
     * 根据ID删除向量
     */
    @Override
    public boolean deleteVector(String collectionName, String id) {
        return deleteVectors(collectionName, Collections.singletonList(id)) == 1;
    }

    /**
     * 批量删除向量
     */
    @Override
    public int deleteVectors(String collectionName, List<String> ids) {
        try {
            // 构建请求URL
            String url = String.format("%s/collections/%s/points/delete", config.getHttpUrl(), collectionName);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("points", ids);

            // 发送请求
            Map<String, String> headers = createHeaders();
            String response = HttpClient.postJson(url, JSONUtil.toJsonStr(requestBody), headers);

            // 简单检查响应不为空即认为成功
            if (StrUtil.isNotBlank(response)) {
                return ids.size();
            } else {
                log.error("删除向量失败: 响应为空");
                return 0;
            }
        } catch (Exception e) {
            log.error("删除向量失败", e);
            return 0;
        }
    }

    /**
     * 根据过滤条件删除向量
     */
    @Override
    public int deleteVectorsByFilter(String collectionName, Map<String, Object> filter) {
        try {
            // 构建请求URL
            String url = String.format("%s/collections/%s/points/delete", config.getHttpUrl(), collectionName);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();

            Map<String, Object> filterMap = new HashMap<>();
            if (filter != null && !filter.isEmpty()) {
                filterMap.put("must", buildFilterCondition(filter));
            }

            requestBody.put("filter", filterMap);

            // 发送请求
            Map<String, String> headers = createHeaders();
            String jsonBody = JSONUtil.toJsonStr(requestBody);
            log.info("删除向量请求体: {}", jsonBody);
            String response = HttpClient.postJson(url, jsonBody, headers);

            // 简单检查响应不为空即认为操作成功，返回1表示操作成功
            if (StrUtil.isNotBlank(response)) {
                return 1;
            } else {
                log.error("删除向量失败: 响应为空");
                return 0;
            }
        } catch (Exception e) {
            log.error("删除向量失败", e);
            return 0;
        }
    }

    /**
     * 执行相似度搜索
     */
    @Override
    protected List<QueryResult> doSimilaritySearch(VectorQuery query) {
        try {
            // 构建请求URL
            String url =
                    String.format("%s/collections/%s/points/search", config.getHttpUrl(), query.getCollectionName());

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            // 对于命名向量配置的集合，需要指定向量名称
            Map<String, Object> namedVector = new HashMap<>();
            namedVector.put("vector", query.getQueryVector());
            namedVector.put("name", config.getVectorName());
            requestBody.put("vector", namedVector);
            requestBody.put("limit", query.getLimit());

            // 添加过滤条件
            if (query.getFilter() != null && !query.getFilter().isEmpty()) {
                Map<String, Object> filterMap = new HashMap<>();
                filterMap.put("must", buildFilterCondition(query.getFilter()));
                requestBody.put("filter", filterMap);
            }

            // 设置是否包含向量数据
            requestBody.put("with_vector", query.isIncludeVectors());
            // 默认返回payload
            requestBody.put("with_payload", true);

            // 发送请求
            Map<String, String> headers = createHeaders();
            String response = HttpClient.postJson(url, JSONUtil.toJsonStr(requestBody), headers);

            // 解析响应
            if (StrUtil.isNotBlank(response)) {
                List<QueryResult> results = new ArrayList<>();
                JSONObject responseJson = JSONUtil.parseObj(response);
                List<JSONObject> resultList = responseJson.getBeanList("result", JSONObject.class);

                for (JSONObject resultItem : resultList) {
                    String id = resultItem.getStr("id");
                    float score = resultItem.getFloat("score");

                    JSONObject payload = resultItem.getJSONObject("payload");
                    String text = payload != null ? payload.getStr("text", "") : "";

                    // 创建元数据副本，移除文本字段
                    Map<String, Object> metadata = new HashMap<>();
                    if (payload != null) {
                        metadata.putAll(payload);
                        metadata.remove("text");
                    }

                    // 如果需要向量数据
                    float[] vector = null;
                    if (query.isIncludeVectors() && resultItem.containsKey("vector")) {
                        Object vectorData = resultItem.get("vector");
                        List<Number> vectorList;

                        // 处理不同格式的向量数据
                        if (vectorData instanceof Map) {
                            // 命名向量格式: {"vectorName": [0.1, 0.2, ...]}
                            Map<String, Object> vectorMap = (Map<String, Object>) vectorData;
                            vectorList = (List<Number>) vectorMap.get(config.getVectorName());
                        } else if (vectorData instanceof List) {
                            // 直接向量数组格式: [0.1, 0.2, ...]
                            vectorList = (List<Number>) vectorData;
                        } else {
                            vectorList = null;
                        }

                        if (vectorList != null) {
                            vector = new float[vectorList.size()];
                            for (int i = 0; i < vectorList.size(); i++) {
                                vector[i] = vectorList.get(i).floatValue();
                            }
                        }
                    }

                    // 只有分数超过阈值的结果才会返回
                    if (score >= query.getMinScore()) {
                        results.add(QueryResult.builder().id(id).text(text).score(score).vector(vector)
                                .metadata(metadata).build());
                    }
                }

                return results;
            } else {
                log.error("相似度搜索失败: 响应为空");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("相似度搜索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 创建集合
     */
    @Override
    public boolean createCollection(String collectionName, int dimension) {
        try {
            // 检查集合是否已存在
            if (collectionExists(collectionName)) {
                return true;
            }

            // 构建请求URL
            String url = String.format("%s/collections/%s", config.getHttpUrl(), collectionName);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();

            // 使用命名向量配置，创建指定名称的向量，与搜索时保持一致
            Map<String, Object> vectorConfig = new HashMap<>();
            vectorConfig.put("size", dimension);
            vectorConfig.put("distance", "Cosine"); // 使用余弦相似度

            Map<String, Object> vectorsConfig = new HashMap<>();
            vectorsConfig.put(config.getVectorName(), vectorConfig);
            requestBody.put("vectors", vectorsConfig);

            // 发送请求
            Map<String, String> headers = createHeaders();
            String response = HttpClient.putJson(url, JSONUtil.toJsonStr(requestBody), headers);

            // 简单检查响应不为空即认为成功
            return StrUtil.isNotBlank(response);
        } catch (Exception e) {
            log.error("创建集合失败", e);
            return false;
        }
    }

    /**
     * 删除集合
     */
    @Override
    public boolean deleteCollection(String collectionName) {
        try {
            // 构建请求URL
            String url = String.format("%s/collections/%s", config.getHttpUrl(), collectionName);

            // 发送请求
            Map<String, String> headers = createHeaders();
            String response = HttpClient.delete(url, headers);

            // 简单检查响应不为空即认为成功
            return StrUtil.isNotBlank(response);
        } catch (Exception e) {
            log.error("删除集合失败", e);
            return false;
        }
    }

    /**
     * 检查集合是否存在
     */
    @Override
    public boolean collectionExists(String collectionName) {
        try {
            // 构建请求URL
            String url = String.format("%s/collections/%s", config.getHttpUrl(), collectionName);

            // 发送请求
            Map<String, String> headers = createHeaders();
            String response = HttpClient.get(url, headers);

            // 简单检查响应不为空即认为集合存在
            return StrUtil.isNotBlank(response);
        } catch (Exception e) {
            // 请求异常，说明集合不存在
            return false;
        }
    }

    /**
     * 验证集合的向量配置是否与当前配置匹配
     */
    public boolean validateCollectionVectorConfig(String collectionName) {
        try {
            // 构建请求URL
            String url = String.format("%s/collections/%s", config.getHttpUrl(), collectionName);

            // 发送请求
            Map<String, String> headers = createHeaders();
            String response = HttpClient.get(url, headers);

            if (StrUtil.isNotBlank(response)) {
                JSONObject responseJson = JSONUtil.parseObj(response);
                JSONObject result = responseJson.getJSONObject("result");
                JSONObject config = result.getJSONObject("config");
                JSONObject params = config.getJSONObject("params");
                JSONObject vectors = params.getJSONObject("vectors");

                // 检查是否包含配置的向量名称
                boolean hasConfiguredVector = vectors.containsKey(this.config.getVectorName());

                if (hasConfiguredVector) {
                    log.info("集合 {} 包含向量配置: {}", collectionName, this.config.getVectorName());
                } else {
                    log.warn("集合 {} 不包含向量配置: {}，现有向量: {}", collectionName, this.config.getVectorName(),
                            vectors.keySet());
                }

                return hasConfiguredVector;
            }

            return false;
        } catch (Exception e) {
            log.error("验证集合向量配置失败: {}", collectionName, e);
            return false;
        }
    }

    /**
     * 验证配置参数
     */
    private void validateConfig() {
        if (config == null) {
            throw new IllegalArgumentException("QdrantConfig 不能为空");
        }

        if (StrUtil.isBlank(config.getVectorName())) {
            throw new IllegalArgumentException("向量名称不能为空");
        }

        if (StrUtil.isBlank(config.getDefaultCollectionName())) {
            throw new IllegalArgumentException("默认集合名称不能为空");
        }

        log.info("QDrant配置验证通过 - 向量名称: {}, 默认集合: {}", config.getVectorName(), config.getDefaultCollectionName());
    }

    /**
     * 创建带认证的HTTP头
     */
    private Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();

        // 如果配置了API密钥，添加到头信息中
        if (StrUtil.isNotBlank(config.getApiKey())) {
            headers.put("api-key", config.getApiKey());
        }

        return headers;
    }

    /**
     * 构建QDrant的过滤条件
     */
    private List<Map<String, Object>> buildFilterCondition(Map<String, Object> filter) {
        List<Map<String, Object>> conditions = new ArrayList<>();

        for (Map.Entry<String, Object> entry : filter.entrySet()) {

            Map<String, Object> matchCondition = new HashMap<>();
            matchCondition.put("key", entry.getKey());
            matchCondition.put("match", Collections.singletonMap("value", entry.getValue()));

            conditions.add(matchCondition);
        }

        return conditions;
    }
}
