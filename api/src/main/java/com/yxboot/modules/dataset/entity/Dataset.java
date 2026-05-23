package com.yxboot.modules.dataset.entity;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;
import com.yxboot.modules.dataset.enums.DatasetStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库表实体类
 *
 * @author Boya
 */
@Data
@Table(value = "dataset", onInsert = MyFlexListener.class, onUpdate = MyFlexListener.class)
@Schema(description = "知识库信息")
public class Dataset {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Schema(description = "知识库ID")
    private Long datasetId;

    @Schema(description = "所属租户ID")
    private Long tenantId;

    @Schema(description = "知识库名称")
    private String datasetName;

    @Schema(description = "知识库描述")
    private String datasetDesc;

    @Schema(description = "状态")
    private DatasetStatus status;

    @Schema(description = "向量化使用的模型标识，格式 provider:model")
    private String embeddingModel;

    @Schema(description = "创建者ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者ID")
    private Long updatorId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
