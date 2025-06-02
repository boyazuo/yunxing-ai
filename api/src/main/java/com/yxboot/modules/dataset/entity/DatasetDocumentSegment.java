package com.yxboot.modules.dataset.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档分段实体类
 * 
 * @author Boya
 */
@Data
@TableName("dataset_document_segment")
@Schema(description = "文档分段信息")
public class DatasetDocumentSegment {

    @TableId(value = "segment_id", type = IdType.ASSIGN_ID)
    @Schema(description = "分段ID")
    private Long segmentId;

    @Schema(description = "所属租户ID")
    private Long tenantId;

    @Schema(description = "知识库ID")
    private Long datasetId;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "向量ID")
    private String vectorId;

    @Schema(description = "位置")
    private Integer position;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "内容长度")
    private Integer contentLength;

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
