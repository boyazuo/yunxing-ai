package com.yxboot.modules.ai.dto;

import com.yxboot.modules.ai.entity.Model;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型DTO
 * 
 * @author Boya
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型详细信息")
public class ModelDTO extends Model {

    @Schema(description = "提供商名称")
    private String providerName;

    @Schema(description = "提供商Logo")
    private String providerLogo;

    public static ModelDTO fromModel(Model model) {
        ModelDTO dto = new ModelDTO();
        BeanUtil.copyProperties(model, dto);
        return dto;
    }
}