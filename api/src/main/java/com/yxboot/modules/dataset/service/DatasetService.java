package com.yxboot.modules.dataset.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.ai.config.AiProperties;
import com.yxboot.ai.service.AiVectorStoreService;
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

    private final AiVectorStoreService vectorStoreService;
    private final AiProperties aiProperties;

    public String getCurrentEmbeddingModelKey() {
        return aiProperties.getEmbeddingModelKey();
    }

    /**
     * 判断知识库记录的向量模型是否与当前系统配置一致。
     * null 表示未记录（历史数据或未向量化）。
     */
    public Boolean resolveEmbeddingModelMatched(String embeddingModel) {
        if (!StringUtils.hasText(embeddingModel)) {
            return null;
        }
        return embeddingModel.equals(getCurrentEmbeddingModelKey());
    }

    public void enrichDatasetDTO(DatasetDTO dto) {
        dto.setEmbeddingModelMatched(resolveEmbeddingModelMatched(dto.getEmbeddingModel()));
    }

    public DatasetDTO toDatasetDTO(Dataset dataset) {
        DatasetDTO dto = DatasetDTO.fromDataset(dataset);
        enrichDatasetDTO(dto);
        return dto;
    }

    /**
     * 向量检索前校验：已记录且与当前配置不一致时拒绝检索，避免静默返回错误结果。
     */
    public void ensureEmbeddingModelCompatible(Dataset dataset) {
        if (dataset == null || !StringUtils.hasText(dataset.getEmbeddingModel())) {
            return;
        }
        String currentKey = getCurrentEmbeddingModelKey();
        if (!dataset.getEmbeddingModel().equals(currentKey)) {
            throw new IllegalStateException(
                    "知识库向量模型与当前系统配置不一致，请重新向量化后再检索。"
                            + " 知识库模型: " + dataset.getEmbeddingModel()
                            + ", 当前模型: " + currentKey);
        }
    }

    /**
     * 向量化完成后记录当前系统配置的向量模型标识。
     */
    public void recordEmbeddingModel(Long datasetId) {
        Dataset dataset = getById(datasetId);
        if (dataset == null) {
            return;
        }
        String currentKey = getCurrentEmbeddingModelKey();
        if (currentKey.equals(dataset.getEmbeddingModel())) {
            return;
        }
        dataset.setEmbeddingModel(currentKey);
        updateById(dataset);
        log.info("记录知识库向量模型, datasetId={}, embeddingModel={}", datasetId, currentKey);
    }

    /**
     * 创建知识库
     * 
     * @param tenantId 租户ID
     * @param datasetName 知识库名称
     * @param datasetDesc 知识库描述
     * @return 知识库对象
     */
    public Dataset createDataset(Long tenantId, String datasetName, String datasetDesc) {
        Dataset dataset = new Dataset();
        dataset.setTenantId(tenantId);
        dataset.setDatasetName(datasetName);
        dataset.setDatasetDesc(datasetDesc);
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
        List<DatasetDTO> datasets = baseMapper.getDatasetsByTenantId(tenantId);
        datasets.forEach(this::enrichDatasetDTO);
        return datasets;
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
            Dataset dataset = getById(datasetId);
            if (dataset == null) {
                return false;
            }

            // 1. 删除向量集合
            try {
                vectorStoreService.deleteCollection(datasetId, dataset.getTenantId());
            } catch (Exception e) {
                log.error("删除知识库向量集合失败, datasetId: {}, tenantId: {}", datasetId, dataset.getTenantId(), e);
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
