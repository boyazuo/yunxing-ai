package com.yxboot.modules.dataset.controller;

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
import com.yxboot.modules.dataset.dto.DatasetDTO;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.enums.DatasetStatus;
import com.yxboot.modules.dataset.service.DatasetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 知识库控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/datasets")
@Tag(name = "知识库API", description = "知识库管理相关接口")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;

    @GetMapping
    @Operation(summary = "获取知识库列表", description = "获取当前租户下的所有知识库")
    public Result<List<DatasetDTO>> getDatasets(@Parameter(description = "租户ID") String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "租户ID不能为空");
        }
        List<DatasetDTO> datasets = datasetService.getDatasetsByTenantId(tenantId);
        return Result.success("查询成功", datasets);
    }

    @GetMapping("/{datasetId}")
    @Operation(summary = "获取知识库详情", description = "根据知识库ID获取知识库详情")
    public Result<Dataset> getDatasetById(@PathVariable Long datasetId) {
        Dataset dataset = datasetService.getById(datasetId);
        if (dataset == null) {
            return Result.error(ResultCode.NOT_FOUND, "知识库不存在");
        }
        return Result.success("查询成功", dataset);
    }

    @PostMapping
    @Operation(summary = "创建知识库", description = "创建新的知识库")
    public Result<Dataset> createDataset(@RequestBody DatasetRequest datasetRequest) {
        // 参数验证
        Long tenantId = datasetRequest.getTenantId();
        String datasetName = datasetRequest.getDatasetName();
        String datasetDesc = datasetRequest.getDatasetDesc();
        Long embeddingModelId = datasetRequest.getEmbeddingModelId();

        if (tenantId == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "租户ID不能为空");
        }
        if (datasetName == null || datasetName.trim().isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "知识库名称不能为空");
        }
        if (embeddingModelId == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "嵌入模型ID不能为空");
        }

        // 创建知识库
        Dataset dataset = datasetService.createDataset(tenantId, datasetName, datasetDesc, embeddingModelId);

        return Result.success("知识库创建成功", dataset);
    }

    @PutMapping("/{datasetId}")
    @Operation(summary = "更新知识库", description = "更新知识库信息")
    public Result<Dataset> updateDataset(@PathVariable Long datasetId, @RequestBody DatasetRequest datasetRequest) {
        // 验证知识库是否存在
        Dataset existingDataset = datasetService.getById(datasetId);
        if (existingDataset == null) {
            return Result.error(ResultCode.NOT_FOUND, "知识库不存在");
        }

        // 设置要更新的字段
        if (datasetRequest.getDatasetName() != null) {
            existingDataset.setDatasetName(datasetRequest.getDatasetName());
        }
        if (datasetRequest.getDatasetDesc() != null) {
            existingDataset.setDatasetDesc(datasetRequest.getDatasetDesc());
        }
        if (datasetRequest.getEmbeddingModelId() != null) {
            existingDataset.setEmbeddingModelId(datasetRequest.getEmbeddingModelId());
        }
        if (datasetRequest.getStatus() != null) {
            existingDataset.setStatus(datasetRequest.getStatus());
        }

        // 更新知识库
        boolean updated = datasetService.updateById(existingDataset);
        if (!updated) {
            return Result.error(ResultCode.FAIL, "知识库更新失败");
        }

        return Result.success("知识库更新成功", existingDataset);
    }

    @DeleteMapping("/{datasetId}")
    @Operation(summary = "删除知识库", description = "删除指定知识库")
    public Result<Void> deleteDataset(@PathVariable Long datasetId) {
        // 验证知识库是否存在
        Dataset existingDataset = datasetService.getById(datasetId);
        if (existingDataset == null) {
            return Result.error(ResultCode.NOT_FOUND, "知识库不存在");
        }

        // 删除知识库
        boolean removed = datasetService.removeById(datasetId);
        if (!removed) {
            return Result.error(ResultCode.FAIL, "知识库删除失败");
        }

        return Result.success("知识库已删除");
    }

    @PutMapping("/{datasetId}/status")
    @Operation(summary = "更新知识库状态", description = "更新知识库状态")
    public Result<Void> updateDatasetStatus(@PathVariable Long datasetId, @RequestBody StatusRequest statusRequest) {
        if (statusRequest.getStatus() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "状态不能为空");
        }

        boolean updated = datasetService.updateDatasetStatus(datasetId, statusRequest.getStatus());
        if (!updated) {
            return Result.error(ResultCode.FAIL, "知识库状态更新失败");
        }

        return Result.success("知识库状态已更新");
    }

    @Data
    public static class DatasetRequest {
        private Long tenantId;
        private Long datasetId;
        private String datasetName;
        private String datasetDesc;
        private Long embeddingModelId;
        private DatasetStatus status;
    }

    @Data
    public static class StatusRequest {
        private DatasetStatus status;
    }
}