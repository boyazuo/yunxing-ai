package com.yxboot.modules.ai.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.ai.dto.ModelDTO;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.enums.CommonStatus;
import com.yxboot.modules.ai.enums.ModelType;
import com.yxboot.modules.ai.mapper.ModelMapper;

import lombok.RequiredArgsConstructor;

/**
 * 模型服务实现类
 * 
 * @author Boya
 */
@Service
@RequiredArgsConstructor
public class ModelService extends ServiceImpl<ModelMapper, Model> {

    private final ProviderService providerService;

    /**
     * 创建模型
     * 
     * @param tenantId      租户ID
     * @param providerId    提供商ID
     * @param modelName     模型名称
     * @param displayName   显示名称
     * @param modelType     模型类型
     * @param contextLength 上下文长度
     * @param maxTokens     最大输出token
     * @param inputPrice    输入价格
     * @param outputPrice   输出价格
     * @return 模型
     */
    public Model createModel(Long tenantId, Long providerId, String modelName, String displayName, ModelType modelType,
            Integer contextLength, Integer maxTokens, BigDecimal inputPrice, BigDecimal outputPrice) {
        // 更新提供商最后使用时间
        providerService.updateLastUsedTime(providerId);

        Model model = new Model();
        model.setTenantId(tenantId);
        model.setProviderId(providerId);
        model.setModelName(modelName);
        model.setDisplayName(displayName);
        model.setModelType(modelType);
        model.setContextLength(contextLength);
        model.setMaxTokens(maxTokens);
        model.setInputPrice(inputPrice);
        model.setOutputPrice(outputPrice);
        model.setStatus(CommonStatus.ACTIVE);
        save(model);
        return model;
    }

    /**
     * 获取提供商下的所有模型列表
     * 
     * @param providerId 提供商ID
     * @return 模型列表
     */
    public List<ModelDTO> getModelsByProviderId(Long providerId) {
        return baseMapper.getModelsByProviderId(providerId);
    }

    /**
     * 获取模型类型列表
     * 
     * @param modelType 模型类型
     * @return 模型列表
     */
    public List<ModelDTO> getModelsByType(ModelType modelType) {
        LambdaQueryWrapper<Model> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Model::getModelType, modelType);
        return list(queryWrapper).stream().map(ModelDTO::fromModel).collect(Collectors.toList());
    }

    /**
     * 更新模型信息
     * 
     * @param modelId       模型ID
     * @param modelName     模型名称
     * @param displayName   显示名称
     * @param modelType     模型类型
     * @param contextLength 上下文长度
     * @param maxTokens     最大输出token
     * @param inputPrice    输入价格
     * @param outputPrice   输出价格
     * @param status        状态
     * @return 是否成功
     */
    public boolean updateModel(Long modelId, String modelName, String displayName, ModelType modelType,
            Integer contextLength, Integer maxTokens, BigDecimal inputPrice, BigDecimal outputPrice,
            CommonStatus status) {
        Model model = getById(modelId);
        if (model == null) {
            return false;
        }

        if (modelName != null) {
            model.setModelName(modelName);
        }
        if (displayName != null) {
            model.setDisplayName(displayName);
        }
        if (modelType != null) {
            model.setModelType(modelType);
        }
        if (contextLength != null) {
            model.setContextLength(contextLength);
        }
        if (maxTokens != null) {
            model.setMaxTokens(maxTokens);
        }
        if (inputPrice != null) {
            model.setInputPrice(inputPrice);
        }
        if (outputPrice != null) {
            model.setOutputPrice(outputPrice);
        }
        if (status != null) {
            model.setStatus(status);
        }

        return updateById(model);
    }
}