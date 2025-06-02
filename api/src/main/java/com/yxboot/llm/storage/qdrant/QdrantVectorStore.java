package com.yxboot.llm.storage.qdrant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.storage.AbstractVectorStore;
import com.yxboot.llm.storage.query.QueryResult;
import com.yxboot.llm.storage.query.VectorQuery;

import lombok.extern.slf4j.Slf4j;

/**
 * QDrant向量存储实现
 */
@Slf4j
@SuppressWarnings("unchecked")
public class QdrantVectorStore extends AbstractVectorStore {

    private final QdrantConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     *
     * @param config         配置
     * @param embeddingModel 嵌入模型
     */
    public QdrantVectorStore(QdrantConfig config, EmbeddingModel embeddingModel) {
        super(config.getDefaultCollectionName(), embeddingModel);
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();

        // 确保默认集合存在
        if (!collectionExists(config.getDefaultCollectionName())) {
            createCollection(config.getDefaultCollectionName(), embeddingModel.getEmbeddingDimension());
        }
    }

    /**
     * 添加单个向量
     */
    @Override
    public boolean addVector(String collectionName, String id, float[] vector, Map<String, Object> metadata,
            String text) {
        return addVectors(collectionName,
                Collections.singletonList(id),
                Collections.singletonList(vector),
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
            String url = String.format("%s/collections/%s/points",
                    config.getHttpUrl(),
                    collectionName);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();

            // 准备点位列表
            List<Map<String, Object>> points = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                Map<String, Object> point = new HashMap<>();
                // 使用ID，如果为null则生成UUID
                point.put("id", ids.get(i) != null ? ids.get(i) : UUID.randomUUID().toString());
                point.put("vector", vectors.get(i));

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
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            // 解析响应
            if (response.getStatusCode().is2xxSuccessful()) {
                return ids.size();
            } else {
                log.error("添加向量失败: {}", response.getBody());
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
            String url = String.format("%s/collections/%s/points/delete",
                    config.getHttpUrl(),
                    collectionName);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("points", ids);

            // 发送请求
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 解析响应
            if (response.getStatusCode().is2xxSuccessful()) {
                return ids.size();
            } else {
                log.error("删除向量失败: {}", response.getBody());
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
            String url = String.format("%s/collections/%s/points/delete",
                    config.getHttpUrl(),
                    collectionName);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();

            Map<String, Object> filterMap = new HashMap<>();
            if (filter != null && !filter.isEmpty()) {
                filterMap.put("must", buildFilterCondition(filter));
            }

            requestBody.put("filter", filterMap);

            // 发送请求
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 解析响应，无法确切知道删除了多少向量，所以返回1表示操作成功
            if (response.getStatusCode().is2xxSuccessful()) {
                return 1;
            } else {
                log.error("删除向量失败: {}", response.getBody());
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
            String url = String.format("%s/collections/%s/points/search",
                    config.getHttpUrl(),
                    query.getCollectionName());

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("vector", query.getQueryVector());
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
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 解析响应
            if (response.getStatusCode().is2xxSuccessful()) {
                List<QueryResult> results = new ArrayList<>();
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) responseBody.get("result");

                for (Map<String, Object> resultItem : resultList) {
                    String id = resultItem.get("id").toString();
                    float score = Float.parseFloat(resultItem.get("score").toString());

                    Map<String, Object> payload = (Map<String, Object>) resultItem.get("payload");
                    String text = payload.containsKey("text") ? payload.get("text").toString() : "";

                    // 创建元数据副本，移除文本字段
                    Map<String, Object> metadata = new HashMap<>(payload);
                    metadata.remove("text");

                    // 如果需要向量数据
                    float[] vector = null;
                    if (query.isIncludeVectors() && resultItem.containsKey("vector")) {
                        List<Number> vectorList = (List<Number>) resultItem.get("vector");
                        vector = new float[vectorList.size()];
                        for (int i = 0; i < vectorList.size(); i++) {
                            vector[i] = vectorList.get(i).floatValue();
                        }
                    }

                    // 只有分数超过阈值的结果才会返回
                    if (score >= query.getMinScore()) {
                        results.add(QueryResult.builder()
                                .id(id)
                                .text(text)
                                .score(score)
                                .vector(vector)
                                .metadata(metadata)
                                .build());
                    }
                }

                return results;
            } else {
                log.error("相似度搜索失败: {}", response.getBody());
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
            String url = String.format("%s/collections/%s",
                    config.getHttpUrl(),
                    collectionName);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();

            Map<String, Object> vectorConfig = new HashMap<>();
            vectorConfig.put("size", dimension);
            vectorConfig.put("distance", "Cosine"); // 使用余弦相似度

            requestBody.put("vectors", Collections.singletonMap("default", vectorConfig));

            // 发送请求
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            // 解析响应
            return response.getStatusCode().is2xxSuccessful();
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
            String url = String.format("%s/collections/%s",
                    config.getHttpUrl(),
                    collectionName);

            // 发送请求
            HttpHeaders headers = createHeaders();

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

            // 解析响应
            return response.getStatusCode().is2xxSuccessful();
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
            String url = String.format("%s/collections/%s",
                    config.getHttpUrl(),
                    collectionName);

            // 发送请求
            HttpHeaders headers = createHeaders();

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // 解析响应
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            // 请求异常，说明集合不存在
            return false;
        }
    }

    /**
     * 创建带认证的HTTP头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();

        // 如果配置了API密钥，添加到头信息中
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            headers.set("api-key", config.getApiKey());
        }

        return headers;
    }

    /**
     * 构建QDrant的过滤条件
     */
    private List<Map<String, Object>> buildFilterCondition(Map<String, Object> filter) {
        List<Map<String, Object>> conditions = new ArrayList<>();

        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            Map<String, Object> condition = new HashMap<>();

            Map<String, Object> matchCondition = new HashMap<>();
            matchCondition.put("key", entry.getKey());
            matchCondition.put("match", Collections.singletonMap("value", entry.getValue()));

            condition.put("key", matchCondition);
            conditions.add(condition);
        }

        return conditions;
    }
}