package com.yxboot.modules.dataset.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.dataset.dto.DatasetDTO;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.enums.DatasetStatus;
import com.yxboot.modules.dataset.mapper.DatasetMapper;

import lombok.RequiredArgsConstructor;

/**
 * 数据集服务实现类
 * 
 * @author Boya
 */
@Service
@RequiredArgsConstructor
public class DatasetService extends ServiceImpl<DatasetMapper, Dataset> {

    /**
     * 创建数据集
     * 
     * @param tenantId         租户ID
     * @param datasetName      数据集名称
     * @param datasetDesc      数据集描述
     * @param embeddingModelId 嵌入模型ID
     * @return 数据集对象
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
     * 获取租户下的所有数据集列表
     * 
     * @param tenantId 租户ID
     * @return 数据集列表
     */
    public List<DatasetDTO> getDatasetsByTenantId(String tenantId) {
        return baseMapper.getDatasetsByTenantId(tenantId);
    }

    /**
     * 更新数据集状态
     * 
     * @param datasetId 数据集ID
     * @param status    新状态
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
}