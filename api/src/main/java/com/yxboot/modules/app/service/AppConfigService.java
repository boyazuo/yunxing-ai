package com.yxboot.modules.app.service;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.app.entity.AppConfig;
import com.yxboot.modules.app.mapper.AppConfigMapper;

import lombok.RequiredArgsConstructor;

import static com.yxboot.modules.app.entity.table.AppConfigTableDef.APP_CONFIG;

/**
 * 应用配置服务实现类
 */
@Service
@RequiredArgsConstructor
public class AppConfigService extends ServiceImpl<AppConfigMapper, AppConfig> {

    public AppConfig getByAppId(Long appId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(APP_CONFIG.APP_ID.eq(appId));
        return getOne(wrapper);
    }
}
