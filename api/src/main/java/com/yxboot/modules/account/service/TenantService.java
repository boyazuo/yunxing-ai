package com.yxboot.modules.account.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.account.dto.TenantUserDTO;
import com.yxboot.modules.account.entity.Tenant;
import com.yxboot.modules.account.enums.TenantPlan;
import com.yxboot.modules.account.enums.TenantStatus;
import com.yxboot.modules.account.mapper.TenantMapper;

import lombok.RequiredArgsConstructor;

import static com.yxboot.modules.account.entity.table.TenantTableDef.TENANT;
import static com.yxboot.modules.account.entity.table.TenantUserTableDef.TENANT_USER;

/**
 * 租户服务实现类
 */
@Service
@RequiredArgsConstructor
public class TenantService extends ServiceImpl<TenantMapper, Tenant> {

    public Long createTenant(String tenantName) {
        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantName);
        tenant.setPlan(TenantPlan.FREE);
        tenant.setStatus(TenantStatus.ACTIVE);
        save(tenant);
        return tenant.getTenantId();
    }

    public List<TenantUserDTO> getTenantsByUserId(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.select(TENANT.ALL_COLUMNS);
        wrapper.select(TENANT_USER.ROLE, TENANT_USER.IS_ACTIVE);
        wrapper.from(TENANT);
        wrapper.innerJoin(TENANT_USER).on(TENANT.TENANT_ID.eq(TENANT_USER.TENANT_ID));
        wrapper.where(TENANT_USER.USER_ID.eq(userId));
        return listAs(wrapper, TenantUserDTO.class);
    }
}
