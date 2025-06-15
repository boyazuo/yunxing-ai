package com.yxboot.modules.dataset.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.llm.vector.VectorStore;
import com.yxboot.modules.dataset.dto.DatasetDTO;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.enums.DatasetStatus;
import com.yxboot.modules.dataset.mapper.DatasetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库服务实现类
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetService extends ServiceImpl<DatasetMapper, Dataset> {

    private final VectorStore vectorStore;

    /**
     * 创建知识库
     * 
     * @param tenantId 租户ID
     * @param datasetName 知识库名称
     * @param datasetDesc 知识库描述
     * @param embeddingModelId 嵌入模型ID
     * @return 知识库对象
     */
    public Dataset createDataset(Long tenantId, String datasetName, String datasetDesc, Long embeddingModelId) {
        Dataset dataset = new Dataset();
        dataset.setTenantId(tenantId);
        dataset.setDatasetName(datasetName);
        dataset.setDatasetDesc(datasetDesc);
        dataset.setEmbeddingModelId(embeddingModelId);
        dataset.setStatus(DatasetStatus.ACTIVE);
        save(dataset);
        return dataset;
    }

    /**
     * 获取租户下的所有知识库列表
     * 
     * @param tenantId 租户ID
     * @return 知识库列表
     */
    public List<DatasetDTO> getDatasetsByTenantId(String tenantId) {
        return baseMapper.getDatasetsByTenantId(tenantId);
    }

    /**
     * 更新知识库状态
     * 
     * @param datasetId 知识库ID
     * @param status 新状态
     * @return 是否成功
     */
    public boolean updateDatasetStatus(Long datasetId, DatasetStatus status) {
        Dataset dataset = getById(datasetId);
        if (dataset == null) {
            return false;
        }
        dataset.setStatus(status);
        return updateById(dataset);
    }

    /**
     * 删除知识库及其相关数据（包括向量集合）
     * 
     * @param datasetId 知识库ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDatasetWithVectors(Long datasetId) {
        try {
            // 1. 删除向量集合
            String collectionName = "dataset_" + datasetId;
            try {
                if (vectorStore.collectionExists(collectionName)) {
                    boolean collectionDeleted = vectorStore.deleteCollection(collectionName);
                    if (collectionDeleted) {
                        log.info("删除知识库向量集合成功, datasetId: {}, collectionName: {}", datasetId, collectionName);
                    } else {
                        log.warn("删除知识库向量集合失败, datasetId: {}, collectionName: {}", datasetId, collectionName);
                    }
                } else {
                    log.info("知识库向量集合不存在, datasetId: {}, collectionName: {}", datasetId, collectionName);
                }
            } catch (Exception e) {
                log.error("删除知识库向量集合失败, datasetId: {}, collectionName: {}", datasetId, collectionName, e);
                // 向量集合删除失败不影响数据库删除，只记录日志
            }

            // 2. 删除知识库记录
            removeById(datasetId);
            log.info("知识库删除完成, datasetId: {}", datasetId);
            return true;
        } catch (Exception e) {
            log.error("删除知识库失败, datasetId: {}", datasetId, e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }
}
