package com.yxboot.modules.ai.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.ai.dto.ProviderDTO;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.enums.CommonStatus;
import com.yxboot.modules.ai.mapper.ProviderMapper;

import lombok.RequiredArgsConstructor;

/**
 * 提供商服务实现类
 * 
 * @author Boya
 */
@Service
@RequiredArgsConstructor
public class ProviderService extends ServiceImpl<ProviderMapper, Provider> {

    /**
     * 创建提供商
     * 
     * @param tenantId     租户ID
     * @param providerName 提供商名称
     * @param logo         Logo
     * @param apiKey       API密钥
     * @param endpoint     终端地址
     * @return 提供商
     */
    public Provider createProvider(Long tenantId, String providerName, String logo, String apiKey, String endpoint) {
        Provider provider = new Provider();
        provider.setTenantId(tenantId);
        provider.setProviderName(providerName);
        provider.setLogo(logo);
        provider.setApiKey(apiKey);
        provider.setEndpoint(endpoint);
        provider.setStatus(CommonStatus.ACTIVE);
        provider.setLastUsedTime(LocalDateTime.now());
        save(provider);
        return provider;
    }

    /**
     * 获取租户下的所有提供商列表
     * 
     * @param tenantId 租户ID
     * @return 提供商列表
     */
    public List<ProviderDTO> getProvidersByTenantId(Long tenantId) {
        LambdaQueryWrapper<Provider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Provider::getTenantId, tenantId);
        List<Provider> providers = list(queryWrapper);
        return providers.stream()
                .map(ProviderDTO::fromProvider)
                .collect(Collectors.toList());
    }

    /**
     * 更新提供商信息
     * 
     * @param providerId   提供商ID
     * @param providerName 提供商名称
     * @param logo         Logo
     * @param apiKey       API密钥
     * @param endpoint     终端地址
     * @param status       状态
     * @return 是否成功
     */
    public boolean updateProvider(Long providerId, String providerName, String logo, String apiKey, String endpoint,
            CommonStatus status) {
        Provider provider = getById(providerId);
        if (provider == null) {
            return false;
        }

        if (providerName != null) {
            provider.setProviderName(providerName);
        }
        if (logo != null) {
            provider.setLogo(logo);
        }
        if (apiKey != null) {
            provider.setApiKey(apiKey);
        }
        if (endpoint != null) {
            provider.setEndpoint(endpoint);
        }
        if (status != null) {
            provider.setStatus(status);
        }

        return updateById(provider);
    }

    /**
     * 更新提供商使用时间
     * 
     * @param providerId 提供商ID
     * @return 是否成功
     */
    public boolean updateLastUsedTime(Long providerId) {
        Provider provider = getById(providerId);
        if (provider == null) {
            return false;
        }
        provider.setLastUsedTime(LocalDateTime.now());
        return updateById(provider);
    }
}