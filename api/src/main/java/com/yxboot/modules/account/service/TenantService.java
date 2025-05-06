package com.yxboot.modules.account.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.account.dto.TenantUserDTO;
import com.yxboot.modules.account.entity.Tenant;
import com.yxboot.modules.account.enums.TenantPlan;
import com.yxboot.modules.account.enums.TenantStatus;
import com.yxboot.modules.account.mapper.TenantMapper;

import lombok.RequiredArgsConstructor;

/**
 * 租户服务实现类
 * 
 * @author Boya
 */
@Service
@RequiredArgsConstructor
public class TenantService extends ServiceImpl<TenantMapper, Tenant> {

    /**
     * 创建租户
     * 
     * @param tenantName 租户名称
     * @return 租户ID
     */
    public Long createTenant(String tenantName) {
        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantName);
        tenant.setPlan(TenantPlan.FREE);
        tenant.setStatus(TenantStatus.ACTIVE);
        save(tenant);
        return tenant.getTenantId();
    }

    /**
     * 根据用户ID获取租户列表及用户角色
     * 使用联表查询一次性获取租户信息和角色信息
     * 
     * @param userId 用户ID
     * @return 租户及角色列表
     */
    public List<TenantUserDTO> getTenantsByUserId(Long userId) {
        return baseMapper.selectTenantsByUserId(userId);
    }
}