package com.yxboot.modules.dataset.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.ai.config.AiProperties;
import com.yxboot.ai.service.AiVectorStoreService;
import com.yxboot.modules.dataset.dto.DatasetDTO;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.enums.DatasetStatus;
import com.yxboot.modules.dataset.mapper.DatasetMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.yxboot.modules.account.entity.table.UserTableDef.USER;
import static com.yxboot.modules.dataset.entity.table.DatasetTableDef.DATASET;

/**
 * 知识库服务实现类
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

    @Transactional(rollbackFor = Exception.class)
    public Dataset createDataset(Long tenantId, String datasetName, String datasetDesc) {
        Dataset dataset = new Dataset();
        dataset.setTenantId(tenantId);
        dataset.setDatasetName(datasetName);
        dataset.setDatasetDesc(datasetDesc);
        dataset.setStatus(DatasetStatus.ACTIVE);
        save(dataset);
        vectorStoreService.ensureCollectionExists(dataset.getDatasetId(), tenantId);
        return dataset;
    }

    public List<DatasetDTO> getDatasetsByTenantId(String tenantId) {
        QueryWrapper wrapper = buildDatasetDtoQueryWrapper();
        wrapper.where(DATASET.TENANT_ID.eq(tenantId));
        List<DatasetDTO> datasets = listAs(wrapper, DatasetDTO.class);
        datasets.forEach(this::enrichDatasetDTO);
        return datasets;
    }

    public boolean updateDatasetStatus(Long datasetId, DatasetStatus status) {
        Dataset dataset = getById(datasetId);
        if (dataset == null) {
            return false;
        }
        dataset.setStatus(status);
        return updateById(dataset);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDatasetWithVectors(Long datasetId) {
        try {
            Dataset dataset = getById(datasetId);
            if (dataset == null) {
                return false;
            }

            try {
                vectorStoreService.deleteCollection(datasetId, dataset.getTenantId());
            } catch (Exception e) {
                log.error("删除知识库向量集合失败, datasetId: {}, tenantId: {}", datasetId, dataset.getTenantId(), e);
            }

            removeById(datasetId);
            log.info("知识库删除完成, datasetId: {}", datasetId);
            return true;
        } catch (Exception e) {
            log.error("删除知识库失败, datasetId: {}", datasetId, e);
            throw e;
        }
    }

    private QueryWrapper buildDatasetDtoQueryWrapper() {
        var creator = USER.as("cu");
        var updator = USER.as("uu");

        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.select(DATASET.ALL_COLUMNS);
        wrapper.select(creator.USERNAME.as("creatorUsername"));
        wrapper.select(creator.AVATAR.as("creatorAvatar"));
        wrapper.select(updator.USERNAME.as("updatorUsername"));
        wrapper.from(DATASET);
        wrapper.leftJoin(creator).on(DATASET.CREATOR_ID.eq(creator.USER_ID));
        wrapper.leftJoin(updator).on(DATASET.UPDATOR_ID.eq(updator.USER_ID));
        return wrapper;
    }
}
