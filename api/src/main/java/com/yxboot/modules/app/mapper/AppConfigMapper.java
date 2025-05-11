package com.yxboot.modules.app.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxboot.modules.app.entity.AppConfig;

/**
 * 应用配置Mapper接口
 */
@Mapper
public interface AppConfigMapper extends BaseMapper<AppConfig> {
}