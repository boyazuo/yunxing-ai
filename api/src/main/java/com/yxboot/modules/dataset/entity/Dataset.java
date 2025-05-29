package com.yxboot.modules.dataset.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yxboot.modules.dataset.enums.DatasetStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库表实体类
 * 
 * @author Boya
 */
@Data
@TableName("dataset")
@Schema(description = "知识库信息")
public class Dataset {

    @TableId(value = "dataset_id", type = IdType.ASSIGN_ID)
    @Schema(description = "知识库ID")
    private Long datasetId;

    @Schema(description = "所属租户ID")
    private Long tenantId;

    @Schema(description = "知识库名称")
    private String datasetName;

    @Schema(description = "知识库描述")
    private String datasetDesc;

    @Schema(description = "嵌入模型ID")
    private Long embeddingModelId;

    @Schema(description = "状态")
    private DatasetStatus status;

    @Schema(description = "创建者ID")
    @TableField(fill = FieldFill.INSERT)
    private Long creatorId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新者ID")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatorId;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}