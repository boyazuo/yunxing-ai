package com.yxboot.modules.ai.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yxboot.modules.ai.enums.CommonStatus;
import com.yxboot.modules.ai.enums.ModelType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模型实体类
 * 
 * @author Boya
 */
@Data
@TableName("model")
@Schema(description = "模型信息")
public class Model {

    @TableId(value = "model_id", type = IdType.ASSIGN_ID)
    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "所属租户ID")
    private Long tenantId;

    @Schema(description = "提供商ID")
    private Long providerId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "显示名称")
    private String displayName;

    @Schema(description = "模型类型")
    private ModelType modelType;

    @Schema(description = "上下文长度")
    private Integer contextLength;

    @Schema(description = "最大输出token")
    private Integer maxTokens;

    @Schema(description = "输入价格")
    private BigDecimal inputPrice;

    @Schema(description = "输出价格")
    private BigDecimal outputPrice;

    @Schema(description = "状态")
    private CommonStatus status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}