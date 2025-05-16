package com.yxboot.modules.account.controller;

import java.util.List;

import com.yxboot.modules.account.dto.UserInTenantDTO;
import com.yxboot.modules.account.entity.User;
import com.yxboot.modules.account.enums.TenantUserRole;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.common.exception.ApiException;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.account.dto.TenantUserDTO;
import com.yxboot.modules.account.entity.Tenant;
import com.yxboot.modules.account.entity.TenantUser;
import com.yxboot.modules.account.enums.TenantPlan;
import com.yxboot.modules.account.service.TenantService;
import com.yxboot.modules.account.service.TenantUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 租户管理控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/tenants")
@Tag(name = "租户管理API", description = "租户管理相关的接口")
@RequiredArgsConstructor
@Validated
public class TenantController {

    private final TenantService tenantService;
    private final TenantUserService tenantUserService;

    /**
     * 获取当前用户的所有租户列表
     */
    @GetMapping
    @Operation(summary = "获取租户列表", description = "获取当前登录用户的所有租户列表")
    public Result<List<TenantUserDTO>> getTenants(@AuthenticationPrincipal SecurityUser securityUser) {
        Long userId = securityUser.getUserId();
        List<TenantUserDTO> tenants = tenantService.getTenantsByUserId(userId);

        // 获取每个租户的成员数量
        tenants.parallelStream().forEach(item -> {
            Long count = tenantUserService
                    .lambdaQuery()
                    .eq(TenantUser::getTenantId, item.getTenantId())
                    .count();
            item.setMemberCount(count);
        });

        return Result.success("获取租户列表成功", tenants);
    }

    /**
     * 更新租户活跃状态
     */
    @PutMapping("/active")
    @Operation(summary = "更新租户活跃状态", description = "设置用户当前活跃的租户")
    public Result<Void> updateActiveTenant(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody TenantIdRequest tenantIdRequest) {

        Long userId = securityUser.getUserId();

        // 验证租户是否存在
        Tenant tenant = tenantService.getById(tenantIdRequest.getTenantId());
        if (tenant == null) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "租户不存在");
        }

        // 验证用户是否属于该租户
        TenantUser tenantUser = tenantUserService.getTenantUser(userId, tenant.getTenantId());
        if (tenantUser == null) {
            throw new ApiException(ResultCode.FORBIDDEN, "您不是该租户的成员");
        }

        // 更新租户活跃状态
        boolean success = tenantUserService.updateActiveTenant(userId, tenant.getTenantId());
        if (!success) {
            return Result.error("更新租户活跃状态失败");
        }

        return Result.success("更新租户活跃状态成功");
    }

    /**
     * 更新租户信息
     */
    @PutMapping
    @Operation(summary = "更新租户信息", description = "更新指定租户的基本信息")
    public Result<Tenant> updateTenant(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody TenantRequest tenantRequest) {

        Long userId = securityUser.getUserId();

        // 验证租户是否存在
        Tenant tenant = tenantService.getById(tenantRequest.getTenantId());
        if (tenant == null) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "租户不存在");
        }

        // 验证用户是否属于该租户，且有权限修改
        TenantUser tenantUser = tenantUserService.getTenantUser(userId, tenantRequest.getTenantId());
        if (tenantUser == null) {
            throw new ApiException(ResultCode.FORBIDDEN, "您不是该租户的成员");
        }

        // 更新租户信息
        if (tenantRequest.getTenantName() != null) {
            tenant.setTenantName(tenantRequest.getTenantName());
        }
        if (tenantRequest.getPlan() != null) {
            tenant.setPlan(tenantRequest.getPlan());
        }

        boolean success = tenantService.updateById(tenant);
        if (!success) {
            return Result.error("更新租户信息失败");
        }

        return Result.success("更新租户信息成功", tenant);
    }

    @Operation(summary = "获取租户下的用户", description = "获取租户下所拥有的用户")
    @GetMapping("/{tenantId}/users")
    public Result<List<UserInTenantDTO>> getTenantUsers(@PathVariable Long tenantId) {
        List<UserInTenantDTO> userInTenantDTOs = tenantUserService.getUserInTenant(tenantId);
        return Result.success("获取租户下的用户成功", userInTenantDTOs);
    }

    @PutMapping("/{tenantId}/users/{userId}")
    @Operation(summary = "更新租户用户角色", description = "更新租户用户角色")
    public Result<Void> updateTenantUserRole(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @RequestBody TenantUserRoleRequest tenantUserRoleRequest) {

        tenantUserService
                .lambdaUpdate()
                .eq(TenantUser::getTenantId, tenantId)
                .eq(TenantUser::getUserId, userId)
                .set(TenantUser::getRole, tenantUserRoleRequest.getRole())
                .update();

        return Result.success("更新租户用户角色成功");
    }

    @Operation(summary = "删除租户用户", description = "删除租户用户")
    @DeleteMapping("/{tenantId}/users/{userId}")
    public Result<Void> deleteTenantUser(
            @PathVariable Long tenantId,
            @PathVariable Long userId) {
        TenantUser deleteOne = tenantUserService
                .lambdaQuery()
                .eq(TenantUser::getTenantId, tenantId)
                .eq(TenantUser::getUserId, userId)
                .one();

        if (deleteOne != null) {
            boolean success = tenantUserService.removeById(deleteOne);
            if (!success) {
                return Result.error("删除租户用户失败");
            }
        }
        return Result.success("删除租户用户成功");
    }

    @Data
    public static class TenantIdRequest {
        private Long tenantId;
    }

    @Data
    public static class TenantRequest {
        private Long tenantId;
        private String tenantName;
        private TenantPlan plan;
    }

    @Data
    public static class TenantUserRoleRequest {
        private TenantUserRole role;
    }

}