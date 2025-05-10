package com.yxboot.modules.ai.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.modules.ai.dto.ModelDTO;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.enums.CommonStatus;
import com.yxboot.modules.ai.enums.ModelType;
import com.yxboot.modules.ai.service.ModelService;
import com.yxboot.modules.ai.service.ProviderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 模型控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/models")
@Tag(name = "模型API", description = "模型管理相关接口")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;
    private final ProviderService providerService;

    @GetMapping("/provider/{providerId}")
    @Operation(summary = "获取提供商模型列表", description = "获取指定提供商下的模型")
    public Result<List<ModelDTO>> getModelsByProvider(@PathVariable Long providerId) {
        List<ModelDTO> models = modelService.getModelsByProviderId(providerId);
        return Result.success("查询成功", models);
    }

    @GetMapping("/{modelId}")
    @Operation(summary = "获取模型详情", description = "根据模型ID获取详情")
    public Result<ModelDTO> getModelById(@PathVariable Long modelId) {
        Model model = modelService.getById(modelId);
        if (model == null) {
            return Result.error(ResultCode.NOT_FOUND, "模型不存在");
        }
        return Result.success("查询成功", ModelDTO.fromModel(model));
    }

    @PostMapping
    @Operation(summary = "创建模型", description = "创建新的模型")
    public Result<Model> createModel(@RequestBody ModelRequest modelRequest) {
        // 参数验证
        Long tenantId = modelRequest.getTenantId();
        Long providerId = modelRequest.getProviderId();
        String modelName = modelRequest.getModelName();
        String displayName = modelRequest.getDisplayName();
        ModelType modelType = modelRequest.getModelType();
        Integer contextLength = modelRequest.getContextLength();
        Integer maxTokens = modelRequest.getMaxTokens();
        BigDecimal inputPrice = modelRequest.getInputPrice();
        BigDecimal outputPrice = modelRequest.getOutputPrice();

        if (tenantId == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "租户ID不能为空");
        }
        if (providerId == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "提供商ID不能为空");
        }
        if (modelName == null || modelName.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "模型名称不能为空");
        }
        if (modelType == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "模型类型不能为空");
        }

        // 验证提供商是否存在
        Provider provider = providerService.getById(providerId);
        if (provider == null) {
            return Result.error(ResultCode.NOT_FOUND, "提供商不存在");
        }

        // 创建模型
        Model model = modelService.createModel(tenantId, providerId, modelName, displayName, modelType,
                contextLength, maxTokens, inputPrice, outputPrice);

        return Result.success("模型创建成功", model);
    }

    @PutMapping("/{modelId}")
    @Operation(summary = "更新模型", description = "更新模型信息")
    public Result<Model> updateModel(@PathVariable Long modelId, @RequestBody ModelRequest modelRequest) {
        // 验证模型是否存在
        Model existingModel = modelService.getById(modelId);
        if (existingModel == null) {
            return Result.error(ResultCode.NOT_FOUND, "模型不存在");
        }

        // 更新模型
        boolean updated = modelService.updateModel(
                modelId,
                modelRequest.getModelName(),
                modelRequest.getDisplayName(),
                modelRequest.getModelType(),
                modelRequest.getContextLength(),
                modelRequest.getMaxTokens(),
                modelRequest.getInputPrice(),
                modelRequest.getOutputPrice(),
                modelRequest.getStatus());

        if (!updated) {
            return Result.error(ResultCode.FAIL, "模型更新失败");
        }

        return Result.success("模型更新成功", modelService.getById(modelId));
    }

    @DeleteMapping("/{modelId}")
    @Operation(summary = "删除模型", description = "删除指定模型")
    public Result<Void> deleteModel(@PathVariable Long modelId) {
        // 验证模型是否存在
        Model existingModel = modelService.getById(modelId);
        if (existingModel == null) {
            return Result.error(ResultCode.NOT_FOUND, "模型不存在");
        }

        // 删除模型
        boolean removed = modelService.removeById(modelId);
        if (!removed) {
            return Result.error(ResultCode.FAIL, "模型删除失败");
        }

        return Result.success("模型已删除");
    }

    @Data
    public static class ModelRequest {
        private Long tenantId;
        private Long providerId;
        private String modelName;
        private String displayName;
        private ModelType modelType;
        private Integer contextLength;
        private Integer maxTokens;
        private BigDecimal inputPrice;
        private BigDecimal outputPrice;
        private CommonStatus status;
    }
}