package com.yxboot.modules.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.app.dto.AppDTO;
import com.yxboot.modules.app.entity.App;
import com.yxboot.modules.app.enums.AppStatus;
import com.yxboot.modules.app.enums.AppType;
import com.yxboot.modules.app.mapper.AppMapper;

import lombok.RequiredArgsConstructor;

import static com.yxboot.modules.account.entity.table.UserTableDef.USER;
import static com.yxboot.modules.app.entity.table.AppTableDef.APP;

/**
 * 应用服务实现类
 */
@Service
@RequiredArgsConstructor
public class AppService extends ServiceImpl<AppMapper, App> {

    public App createApp(Long tenantId, String appName, String intro, String logo, String logoBackground,
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

    public List<AppDTO> getAppsByTenantId(String tenantId) {
        QueryWrapper wrapper = buildAppDtoQueryWrapper();
        wrapper.where(APP.TENANT_ID.eq(tenantId));
        return listAs(wrapper, AppDTO.class);
    }

    public boolean updateAppStatus(Long appId, AppStatus status) {
        App app = getById(appId);
        if (app == null) {
            return false;
        }
        app.setStatus(status);
        return updateById(app);
    }

    private QueryWrapper buildAppDtoQueryWrapper() {
        var creator = USER.as("cu");
        var updator = USER.as("uu");

        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.select(APP.ALL_COLUMNS);
        wrapper.select(creator.USERNAME.as("creatorUsername"));
        wrapper.select(creator.AVATAR.as("creatorAvatar"));
        wrapper.select(updator.USERNAME.as("updatorUsername"));
        wrapper.from(APP);
        wrapper.leftJoin(creator).on(APP.CREATOR_ID.eq(creator.USER_ID));
        wrapper.leftJoin(updator).on(APP.UPDATOR_ID.eq(updator.USER_ID));
        return wrapper;
    }
}
