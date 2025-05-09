package com.yxboot.modules.ai.dto;

import com.yxboot.modules.ai.entity.Provider;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提供商DTO
 * 
 * @author Boya
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "提供商详细信息")
public class ProviderDTO extends Provider {

    public static ProviderDTO fromProvider(Provider provider) {
        ProviderDTO dto = new ProviderDTO();
        BeanUtil.copyProperties(provider, dto);
        return dto;
    }

}