package com.yxboot.modules.app.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.app.entity.AppConfig;
import com.yxboot.modules.app.mapper.AppConfigMapper;

import lombok.RequiredArgsConstructor;

/**
 * 应用配置服务实现类
 */
@Service
@RequiredArgsConstructor
public class AppConfigService extends ServiceImpl<AppConfigMapper, AppConfig> {

    /**
     * 根据应用ID查询配置
     *
     * @param appId 应用ID
     * @return 应用配置
     */
    public AppConfig getByAppId(Long appId) {
        return lambdaQuery().eq(AppConfig::getAppId, appId).one();
    }
}