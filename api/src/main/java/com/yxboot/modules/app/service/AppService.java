package com.yxboot.modules.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.app.entity.App;
import com.yxboot.modules.app.enums.AppStatus;
import com.yxboot.modules.app.enums.AppType;
import com.yxboot.modules.app.mapper.AppMapper;

import lombok.RequiredArgsConstructor;

/**
 * 应用服务实现类
 * 
 * @author Boya
 */
@Service
@RequiredArgsConstructor
public class AppService extends ServiceImpl<AppMapper, App> {

    /**
     * 创建应用
     * 
     * @param tenantId       租户ID
     * @param appName        应用名称
     * @param intro          应用介绍
     * @param logo           应用Logo
     * @param logoBackground 应用Logo背景色
     * @param type           应用类型
     * @return 应用ID
     */
    public App createApp(String tenantId, String appName, String intro, String logo, String logoBackground,
            AppType type) {
        App app = new App();
        app.setTenantId(tenantId);
        app.setAppName(appName);
        app.setIntro(intro);
        app.setLogo(logo);
        app.setLogoBackground(logoBackground);
        app.setType(type);
        app.setStatus(AppStatus.DRAFT);
        save(app);
        return app;
    }

    /**
     * 获取租户下的所有应用列表
     * 
     * @param tenantId 租户ID
     * @return 应用列表
     */
    public List<App> getAppsByTenantId(String tenantId) {
        LambdaQueryWrapper<App> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(App::getTenantId, tenantId);
        return list(queryWrapper);
    }

    /**
     * 更新应用状态
     * 
     * @param appId  应用ID
     * @param status 新状态
     * @return 是否成功
     */
    public boolean updateAppStatus(Long appId, AppStatus status) {
        App app = getById(appId);
        if (app == null) {
            return false;
        }
        app.setStatus(status);
        return updateById(app);
    }
}