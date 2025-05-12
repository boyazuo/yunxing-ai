package com.yxboot.modules.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.modules.app.dto.AppConfigDTO;
import com.yxboot.modules.app.dto.AppConfigRequest;
import com.yxboot.modules.app.entity.AppConfig;
import com.yxboot.modules.app.service.AppConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 应用配置控制器
 * 
 * @author YunxingAI
 */
@RestController
@RequestMapping("/v1/api/app-configs")
@Tag(name = "应用配置API", description = "应用配置管理相关接口")
@RequiredArgsConstructor
public class AppConfigController {

    private final AppConfigService appConfigService;

    @GetMapping("/app/{appId}")
    @Operation(summary = "获取应用配置", description = "根据应用ID获取配置详情")
    public Result<AppConfigDTO> getConfigByAppId(@PathVariable Long appId) {
        AppConfig config = appConfigService.getByAppId(appId);
        if (config == null) {
            return Result.error(ResultCode.NOT_FOUND, "应用配置不存在");
        }

        // 转换为DTO
        AppConfigDTO dto = new AppConfigDTO();
        dto.setConfigId(config.getConfigId());
        dto.setAppId(config.getAppId());
        dto.setTenantId(config.getTenantId());
        dto.setSysPrompt(config.getSysPrompt());
        dto.setModels(config.getModels());
        dto.setVariables(config.getVariables());
        dto.setDatasets(config.getDatasets());
        dto.setCreateTime(config.getCreateTime());
        dto.setUpdateTime(config.getUpdateTime());

        return Result.success("查询成功", dto);
    }

    @PostMapping
    @Operation(summary = "创建应用配置", description = "创建新的应用配置")
    public Result<AppConfigDTO> createConfig(@Valid @RequestBody AppConfigRequest request) {
        // 检查是否已存在该应用的配置
        AppConfig existingConfig = appConfigService.getByAppId(request.getAppId());
        if (existingConfig != null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "该应用已存在配置");
        }

        // 创建新配置
        AppConfig config = new AppConfig();
        config.setAppId(request.getAppId());
        config.setSysPrompt(request.getSysPrompt());
        config.setModels(request.getModels());
        config.setVariables(request.getVariables());
        config.setDatasets(request.getDatasets());

        appConfigService.save(config);

        // 转换为DTO
        AppConfigDTO dto = new AppConfigDTO();
        dto.setConfigId(config.getConfigId());
        dto.setAppId(config.getAppId());
        dto.setTenantId(config.getTenantId());
        dto.setSysPrompt(config.getSysPrompt());
        dto.setModels(config.getModels());
        dto.setVariables(config.getVariables());
        dto.setDatasets(config.getDatasets());
        dto.setCreateTime(config.getCreateTime());
        dto.setUpdateTime(config.getUpdateTime());

        return Result.success("创建成功", dto);
    }

    @PutMapping("/{appId}")
    @Operation(summary = "更新应用配置", description = "更新现有应用配置")
    public Result<AppConfigDTO> updateConfig(@PathVariable Long appId,
            @Valid @RequestBody AppConfigRequest request) {
        // 验证配置是否存在
        AppConfig existingConfig = appConfigService.getByAppId(appId);
        if (existingConfig == null) {
            return Result.error(ResultCode.NOT_FOUND, "应用配置不存在");
        }

        // 确保请求的应用ID与配置的应用ID一致
        if (!existingConfig.getAppId().equals(request.getAppId())) {
            return Result.error(ResultCode.VALIDATE_FAILED, "应用ID不匹配");
        }

        // 更新配置
        existingConfig.setSysPrompt(request.getSysPrompt());
        existingConfig.setModels(request.getModels());
        existingConfig.setVariables(request.getVariables());
        existingConfig.setDatasets(request.getDatasets());

        boolean updated = appConfigService.updateById(existingConfig);
        if (!updated) {
            return Result.error(ResultCode.FAIL, "配置更新失败");
        }

        // 转换为DTO
        AppConfigDTO dto = new AppConfigDTO();
        dto.setConfigId(existingConfig.getConfigId());
        dto.setAppId(existingConfig.getAppId());
        dto.setTenantId(existingConfig.getTenantId());
        dto.setSysPrompt(existingConfig.getSysPrompt());
        dto.setModels(existingConfig.getModels());
        dto.setVariables(existingConfig.getVariables());
        dto.setDatasets(existingConfig.getDatasets());
        dto.setCreateTime(existingConfig.getCreateTime());
        dto.setUpdateTime(existingConfig.getUpdateTime());

        return Result.success("更新成功", dto);
    }
}