package com.yxboot.modules.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.modules.ai.dto.ProviderDTO;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.enums.CommonStatus;
import com.yxboot.modules.ai.service.ProviderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 提供商控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/providers")
@Tag(name = "提供商API", description = "模型提供商管理相关接口")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @GetMapping
    @Operation(summary = "获取提供商列表", description = "获取当前租户下的所有提供商")
    public Result<List<ProviderDTO>> getProviders(@RequestParam @Parameter(description = "租户ID") Long tenantId) {
        List<ProviderDTO> providers = providerService.getProvidersByTenantId(tenantId);
        return Result.success("查询成功", providers);
    }

    @GetMapping("/{providerId}")
    @Operation(summary = "获取提供商详情", description = "根据提供商ID获取详情")
    public Result<ProviderDTO> getProviderById(@PathVariable Long providerId) {
        Provider provider = providerService.getById(providerId);
        if (provider == null) {
            return Result.error(ResultCode.NOT_FOUND, "提供商不存在");
        }
        return Result.success("查询成功", ProviderDTO.fromProvider(provider));
    }

    @PostMapping
    @Operation(summary = "创建提供商", description = "创建新的提供商")
    public Result<Provider> createProvider(@RequestBody ProviderRequest providerRequest) {
        // 参数验证
        Long tenantId = providerRequest.getTenantId();
        String providerName = providerRequest.getProviderName();
        String logo = providerRequest.getLogo();
        String apiKey = providerRequest.getApiKey();
        String endpoint = providerRequest.getEndpoint();

        if (tenantId == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "租户ID不能为空");
        }
        if (providerName == null || providerName.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "提供商名称不能为空");
        }
        if (apiKey == null || apiKey.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "API密钥不能为空");
        }

        // 创建提供商
        Provider provider = providerService.createProvider(tenantId, providerName, logo, apiKey, endpoint);

        return Result.success("提供商创建成功", provider);
    }

    @PutMapping("/{providerId}")
    @Operation(summary = "更新提供商", description = "更新提供商信息")
    public Result<Provider> updateProvider(@PathVariable Long providerId,
            @RequestBody ProviderRequest providerRequest) {
        // 验证提供商是否存在
        Provider existingProvider = providerService.getById(providerId);
        if (existingProvider == null) {
            return Result.error(ResultCode.NOT_FOUND, "提供商不存在");
        }

        // 更新提供商
        boolean updated = providerService.updateProvider(
                providerId,
                providerRequest.getProviderName(),
                providerRequest.getLogo(),
                providerRequest.getApiKey(),
                providerRequest.getEndpoint(),
                providerRequest.getStatus());

        if (!updated) {
            return Result.error(ResultCode.FAIL, "提供商更新失败");
        }

        return Result.success("提供商更新成功", providerService.getById(providerId));
    }

    @DeleteMapping("/{providerId}")
    @Operation(summary = "删除提供商", description = "删除指定提供商")
    public Result<Void> deleteProvider(@PathVariable Long providerId) {
        // 验证提供商是否存在
        Provider existingProvider = providerService.getById(providerId);
        if (existingProvider == null) {
            return Result.error(ResultCode.NOT_FOUND, "提供商不存在");
        }

        // 删除提供商
        boolean removed = providerService.removeById(providerId);
        if (!removed) {
            return Result.error(ResultCode.FAIL, "提供商删除失败");
        }

        return Result.success("提供商已删除");
    }

    @Data
    public static class ProviderRequest {
        private Long tenantId;
        private String providerName;
        private String logo;
        private String apiKey;
        private String endpoint;
        private CommonStatus status;
    }
}