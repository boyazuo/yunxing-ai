package com.yxboot.modules.app.controller;

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
import com.yxboot.modules.app.entity.App;
import com.yxboot.modules.app.enums.AppStatus;
import com.yxboot.modules.app.enums.AppType;
import com.yxboot.modules.app.service.AppService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 应用控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/apps")
@Tag(name = "应用API", description = "应用管理相关接口")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    @GetMapping
    @Operation(summary = "获取应用列表", description = "获取当前租户下的所有应用")
    public Result<List<App>> getApps(@Parameter(description = "租户ID") String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "租户ID不能为空");
        }
        List<App> apps = appService.getAppsByTenantId(tenantId);
        return Result.success("查询成功", apps);
    }

    @GetMapping("/{appId}")
    @Operation(summary = "获取应用详情", description = "根据应用ID获取应用详情")
    public Result<App> getAppById(@PathVariable Long appId) {
        App app = appService.getById(appId);
        if (app == null) {
            return Result.error(ResultCode.NOT_FOUND, "应用不存在");
        }
        return Result.success("查询成功", app);
    }

    @PostMapping
    @Operation(summary = "创建应用", description = "创建新的应用")
    public Result<App> createApp(@RequestBody AppRequest appRequest) {
        // 参数验证
        String tenantId = appRequest.getTenantId();
        String appName = appRequest.getAppName();
        String intro = appRequest.getIntro();
        String logo = appRequest.getLogo();
        AppType type = appRequest.getType();

        // 创建应用
        App app = appService.createApp(tenantId, appName, intro, logo, type);

        return Result.success("应用创建成功", app);
    }

    @PutMapping("/{appId}")
    @Operation(summary = "更新应用", description = "更新应用信息")
    public Result<App> updateApp(@PathVariable Long appId, @RequestBody AppRequest appRequest) {
        // 验证应用是否存在
        App existingApp = appService.getById(appId);
        if (existingApp == null) {
            return Result.error(ResultCode.NOT_FOUND, "应用不存在");
        }

        // 设置要更新的字段
        if (appRequest.getAppName() != null) {
            existingApp.setAppName(appRequest.getAppName());
        }
        if (appRequest.getIntro() != null) {
            existingApp.setIntro(appRequest.getIntro());
        }
        if (appRequest.getLogo() != null) {
            existingApp.setLogo(appRequest.getLogo());
        }
        if (appRequest.getType() != null) {
            existingApp.setType(appRequest.getType());
        }
        if (appRequest.getStatus() != null) {
            existingApp.setStatus(appRequest.getStatus());
        }

        // 更新应用
        boolean updated = appService.updateById(existingApp);
        if (!updated) {
            return Result.error(ResultCode.FAIL, "应用更新失败");
        }

        return Result.success("应用更新成功", existingApp);
    }

    @DeleteMapping("/{appId}")
    @Operation(summary = "删除应用", description = "删除指定应用")
    public Result<Void> deleteApp(@PathVariable Long appId) {
        // 验证应用是否存在
        App existingApp = appService.getById(appId);
        if (existingApp == null) {
            return Result.error(ResultCode.NOT_FOUND, "应用不存在");
        }

        // 删除应用
        boolean removed = appService.removeById(appId);
        if (!removed) {
            return Result.error(ResultCode.FAIL, "应用删除失败");
        }

        return Result.success("应用已删除");
    }

    @Data
    public static class AppRequest {
        private Long appId;
        private String appName;
        private String intro;
        private String logo;
        private AppType type;
        private AppStatus status;
        private String tenantId;
    }
}