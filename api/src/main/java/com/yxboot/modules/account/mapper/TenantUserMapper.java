package com.yxboot.modules.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxboot.modules.account.dto.UserInTenantDTO;
import com.yxboot.modules.account.entity.TenantUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 租户成员表 Mapper 接口
 * 
 * @author Boya
 */
@Mapper
public interface TenantUserMapper extends BaseMapper<TenantUser> {

    @Select("SELECT\n" +
            "\tu.user_id AS userId,\n" +
            "\tu.username AS username,\n" +
            "\tu.email AS email,\n" +
            "\tu.avatar AS avatar,\n" +
            "\ttu.role AS role,\n" +
            "\ttu.is_active AS isActive\n" +
            "FROM\n" +
            "\t`user` u\n" +
            "\tLEFT JOIN tenant_user tu ON tu.user_id = u.user_id\n" +
            "\tLEFT JOIN tenant t ON t.tenant_id = tu.tenant_id \n" +
            "WHERE t.tenant_id = #{tenantId} order by tu.role desc")
    List<UserInTenantDTO> findUserInTenant(@Param("tenantId") Long tenantId);
}