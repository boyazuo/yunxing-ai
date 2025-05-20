package com.yxboot.modules.dataset.dto;

import java.time.LocalDateTime;

import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.dataset.enums.SegmentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据集文档DTO
 * 
 * @author Boya
 */
@Data
@Schema(description = "数据集文档信息DTO")
public class DatasetDocumentDTO {

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "所属租户ID")
    private Long tenantId;

    @Schema(description = "数据集ID")
    private Long datasetId;

    @Schema(description = "数据集名称")
    private String datasetName;

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件大小")
    private Integer fileSize;

    @Schema(description = "分段方式")
    private SegmentMethod segmentMethod;

    @Schema(description = "分段最大长度")
    private Integer maxSegmentLength;

    @Schema(description = "重叠长度")
    private Integer overlapLength;

    @Schema(description = "文档分段数")
    private Integer segmentNum;

    @Schema(description = "状态")
    private DocumentStatus status;

    @Schema(description = "创建者ID")
    private Long creatorId;

    @Schema(description = "创建者用户名")
    private String creatorUsername;

    @Schema(description = "创建者头像")
    private String creatorAvatar;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者ID")
    private Long updatorId;

    @Schema(description = "更新者用户名")
    private String updatorUsername;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}