package com.yxboot.ai.registry;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Component;
import io.qdrant.client.QdrantClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 按知识库集合（tenant_{tid}_dataset_{did}）缓存 Spring AI {@link VectorStore} 实例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStoreRegistry {

    private final QdrantClient qdrantClient;
    private final EmbeddingModel embeddingModel;
    private final ConcurrentHashMap<String, VectorStore> storeCache = new ConcurrentHashMap<>();

    public VectorStore getOrCreate(Long datasetId, Long tenantId) {
        String collectionName = buildCollectionName(datasetId, tenantId);
        return storeCache.computeIfAbsent(collectionName, k -> createVectorStore(collectionName));
    }

    public String buildCollectionName(Long datasetId, Long tenantId) {
        return String.format("tenant_%d_dataset_%d", tenantId, datasetId);
    }

    public void evict(Long datasetId, Long tenantId) {
        storeCache.remove(buildCollectionName(datasetId, tenantId));
    }

    public boolean collectionExists(Long datasetId, Long tenantId) {
        try {
            String name = buildCollectionName(datasetId, tenantId);
            return qdrantClient.collectionExistsAsync(name).get();
        } catch (Exception e) {
            log.error("检查 Qdrant 集合失败, datasetId={}, tenantId={}", datasetId, tenantId, e);
            return false;
        }
    }

    public boolean deleteCollection(Long datasetId, Long tenantId) {
        try {
            String name = buildCollectionName(datasetId, tenantId);
            if (!qdrantClient.collectionExistsAsync(name).get()) {
                return true;
            }
            qdrantClient.deleteCollectionAsync(name).get();
            storeCache.remove(name);
            return true;
        } catch (Exception e) {
            log.error("删除 Qdrant 集合失败, datasetId={}, tenantId={}", datasetId, tenantId, e);
            return false;
        }
    }

    private VectorStore createVectorStore(String collectionName) {
        return QdrantVectorStore.builder(qdrantClient, embeddingModel)
                .collectionName(collectionName)
                .initializeSchema(true)
                .build();
    }
}
