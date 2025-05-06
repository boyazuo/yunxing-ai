package com.yxboot.modules.account.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxboot.modules.account.dto.TenantUserDTO;
import com.yxboot.modules.account.entity.Tenant;

/**
 * 租户表 Mapper 接口
 * 
 * @author Boya
 */
public interface TenantMapper extends BaseMapper<Tenant> {

    /**
     * 查询用户所属的租户列表及用户在租户中的角色
     * 
     * @param userId 用户ID
     * @return 租户及角色列表
     */
    @Select("SELECT t.tenant_id, t.tenant_name, t.plan, t.status, t.create_time, t.update_time, " +
            "tu.role, tu.is_active " +
            "FROM tenant t " +
            "JOIN tenant_user tu ON t.tenant_id = tu.tenant_id " +
            "WHERE tu.user_id = #{userId}")
    List<TenantUserDTO> selectTenantsByUserId(@Param("userId") Long userId);
}