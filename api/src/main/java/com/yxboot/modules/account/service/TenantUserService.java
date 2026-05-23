package com.yxboot.modules.account.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.account.dto.UserInTenantDTO;
import com.yxboot.modules.account.entity.TenantUser;
import com.yxboot.modules.account.enums.TenantUserRole;
import com.yxboot.modules.account.mapper.TenantUserMapper;

import lombok.RequiredArgsConstructor;

import static com.yxboot.modules.account.entity.table.TenantTableDef.TENANT;
import static com.yxboot.modules.account.entity.table.TenantUserTableDef.TENANT_USER;
import static com.yxboot.modules.account.entity.table.UserTableDef.USER;

/**
 * 租户成员服务实现类
 */
@Service
@RequiredArgsConstructor
public class TenantUserService extends ServiceImpl<TenantUserMapper, TenantUser> {

    public boolean addTenantUser(Long tenantId, Long userId, TenantUserRole role) {
        TenantUser tenantUser = new TenantUser();
        tenantUser.setTenantId(tenantId);
        tenantUser.setUserId(userId);
        if (role == null) {
            role = TenantUserRole.NORMAL;
        }
        tenantUser.setIsActive(true);
        tenantUser.setRole(role);
        return save(tenantUser);
    }

    public boolean updateActiveTenant(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return false;
        }

        TenantUser resetEntity = new TenantUser();
        resetEntity.setIsActive(false);
        QueryWrapper resetWrapper = QueryWrapper.create();
        resetWrapper.where(TENANT_USER.USER_ID.eq(userId));
        update(resetEntity, resetWrapper);

        TenantUser activeEntity = new TenantUser();
        activeEntity.setIsActive(true);
        QueryWrapper updateWrapper = QueryWrapper.create();
        updateWrapper.where(TENANT_USER.USER_ID.eq(userId));
        updateWrapper.where(TENANT_USER.TENANT_ID.eq(tenantId));
        return update(activeEntity, updateWrapper);
    }

    public TenantUser getTenantUser(Long userId, Long tenantId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(TENANT_USER.USER_ID.eq(userId));
        wrapper.where(TENANT_USER.TENANT_ID.eq(tenantId));
        return getOne(wrapper);
    }

    public List<UserInTenantDTO> getUserInTenant(Long tenantId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.select(USER.USER_ID, USER.USERNAME, USER.EMAIL, USER.AVATAR);
        wrapper.select(TENANT_USER.ROLE, TENANT_USER.IS_ACTIVE);
        wrapper.from(USER);
        wrapper.leftJoin(TENANT_USER).on(TENANT_USER.USER_ID.eq(USER.USER_ID));
        wrapper.leftJoin(TENANT).on(TENANT.TENANT_ID.eq(TENANT_USER.TENANT_ID));
        wrapper.where(TENANT.TENANT_ID.eq(tenantId));
        wrapper.orderBy(TENANT_USER.ROLE, false);
        return listAs(wrapper, UserInTenantDTO.class);
    }

    public long countByTenantId(Long tenantId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(TENANT_USER.TENANT_ID.eq(tenantId));
        return count(wrapper);
    }

    public void updateRole(Long tenantId, Long userId, TenantUserRole role) {
        TenantUser entity = new TenantUser();
        entity.setRole(role);
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(TENANT_USER.TENANT_ID.eq(tenantId));
        wrapper.where(TENANT_USER.USER_ID.eq(userId));
        update(entity, wrapper);
    }

    public boolean existsByUserIdAndTenantId(Long userId, Long tenantId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(TENANT_USER.USER_ID.eq(userId));
        wrapper.where(TENANT_USER.TENANT_ID.eq(tenantId));
        return count(wrapper) > 0;
    }
}
