package com.yxboot.modules.account.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.account.entity.TenantUser;
import com.yxboot.modules.account.enums.TenantUserRole;
import com.yxboot.modules.account.mapper.TenantUserMapper;

import lombok.RequiredArgsConstructor;

/**
 * 租户成员服务实现类
 * 
 * @author Boya
 */
@Service
@RequiredArgsConstructor
public class TenantUserService extends ServiceImpl<TenantUserMapper, TenantUser> {

    /**
     * 添加租户成员
     *
     * @param tenantUser 租户成员信息
     * @return 成员ID
     */
    public boolean addTenantUser(Long tenantId, Long userId, TenantUserRole role) {
        TenantUser tenantUser = new TenantUser();
        tenantUser.setTenantId(tenantId);
        tenantUser.setUserId(userId);
        // 默认角色为普通成员
        if (role == null) {
            role = TenantUserRole.NORMAL;
        }
        tenantUser.setIsActive(true);
        tenantUser.setRole(role);
        return save(tenantUser);
    }

    /**
     * 更新用户的活跃租户状态
     * 
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 是否更新成功
     */
    public boolean updateActiveTenant(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return false;
        }

        // 首先将该用户的所有租户设置为非活跃
        LambdaUpdateWrapper<TenantUser> resetWrapper = new LambdaUpdateWrapper<>();
        resetWrapper.eq(TenantUser::getUserId, userId)
                .set(TenantUser::getIsActive, false);
        update(resetWrapper);

        // 再设置指定租户的活跃状态
        LambdaUpdateWrapper<TenantUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TenantUser::getUserId, userId)
                .eq(TenantUser::getTenantId, tenantId)
                .set(TenantUser::getIsActive, true);

        return update(updateWrapper);
    }

    /**
     * 获取用户在租户中的成员信息
     * 
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 租户成员信息
     */
    public TenantUser getTenantUser(Long userId, Long tenantId) {
        LambdaQueryWrapper<TenantUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantUser::getUserId, userId)
                .eq(TenantUser::getTenantId, tenantId);
        return getOne(queryWrapper);
    }
}