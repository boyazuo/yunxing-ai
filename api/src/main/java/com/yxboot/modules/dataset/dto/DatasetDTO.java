package com.yxboot.modules.dataset.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.enums.DatasetStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库信息DTO，包含创建者和更新者的用户名
 * 
 * @author Boya
 */
@Data
@Schema(description = "知识库信息DTO")
public class DatasetDTO {

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

    @Schema(description = "嵌入模型名称")
    private String embeddingModelName;

    @Schema(description = "状态")
    private DatasetStatus status;

    @Schema(description = "创建者ID")
    private Long creatorId;

    @Schema(description = "创建者用户名")
    private String creatorUsername;

    @Schema(description = "创建者头像")
    private String creatorAvatar;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新者ID")
    private Long updatorId;

    @Schema(description = "更新者用户名")
    private String updatorUsername;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 从Dataset实体转换为DatasetDTO
     */
    public static DatasetDTO fromDataset(Dataset dataset) {
        DatasetDTO dto = new DatasetDTO();
        dto.setDatasetId(dataset.getDatasetId());
        dto.setTenantId(dataset.getTenantId());
        dto.setDatasetName(dataset.getDatasetName());
        dto.setDatasetDesc(dataset.getDatasetDesc());
        dto.setEmbeddingModelId(dataset.getEmbeddingModelId());
        dto.setStatus(dataset.getStatus());
        dto.setCreatorId(dataset.getCreatorId());
        dto.setCreateTime(dataset.getCreateTime());
        dto.setUpdatorId(dataset.getUpdatorId());
        dto.setUpdateTime(dataset.getUpdateTime());
        return dto;
    }
}