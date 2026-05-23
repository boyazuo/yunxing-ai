package com.yxboot.modules.dataset.entity;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;
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
@Table(value = "dataset_document", onInsert = MyFlexListener.class, onUpdate = MyFlexListener.class)
@Schema(description = "知识库文档信息")
public class DatasetDocument {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
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

    @Schema(description = "文件hash值")
    private String fileHash;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者ID")
    private Long updatorId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
