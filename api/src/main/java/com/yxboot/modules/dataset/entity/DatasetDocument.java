package com.yxboot.modules.dataset.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.dataset.enums.SegmentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库文档实体类
 * 
 * @author Boya
 */
@Data
@TableName("dataset_document")
@Schema(description = "知识库文档信息")
public class DatasetDocument {

    @TableId(value = "document_id", type = IdType.ASSIGN_ID)
    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "所属租户ID")
    private Long tenantId;

    @Schema(description = "知识库ID")
    private Long datasetId;

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