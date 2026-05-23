package com.yxboot.modules.dataset.entity;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档分段实体类
 *
 * @author Boya
 */
@Data
@Table(value = "dataset_document_segment", onInsert = MyFlexListener.class, onUpdate = MyFlexListener.class)
@Schema(description = "文档分段信息")
public class DatasetDocumentSegment {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
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

    @Schema(description = "分段类型：0=普通，1=父块，2=子块")
    private Integer segmentType;

    @Schema(description = "父块分段ID")
    private Long parentSegmentId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "内容长度")
    private Integer contentLength;

    @Schema(description = "创建者ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者ID")
    private Long updatorId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
