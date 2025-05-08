package com.yxboot.modules.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxboot.modules.app.dto.AppDTO;
import com.yxboot.modules.app.entity.App;

/**
 * 应用表 Mapper 接口
 * 
 * @author Boya
 */
public interface AppMapper extends BaseMapper<App> {

    /**
     * 根据租户ID获取应用列表，同时包含创建者和更新者用户名
     * 
     * @param tenantId 租户ID
     * @return 应用列表DTO
     */
    @Select("SELECT a.*, " +
            "cu.username as creator_username, " +
            "cu.avatar as creator_avatar, " +
            "uu.username as updator_username " +
            "FROM app a " +
            "LEFT JOIN user cu ON a.creator_id = cu.user_id " +
            "LEFT JOIN user uu ON a.updator_id = uu.user_id " +
            "WHERE a.tenant_id = #{tenantId}")
    List<AppDTO> getAppsByTenantId(@Param("tenantId") String tenantId);
}