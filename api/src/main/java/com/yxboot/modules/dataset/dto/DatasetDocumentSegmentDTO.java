package com.yxboot.modules.dataset.dto;

import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档分段DTO
 * 
 * @author Boya
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档分段信息DTO")
public class DatasetDocumentSegmentDTO extends DatasetDocumentSegment {

    @Schema(description = "创建者用户名")
    private String creatorUsername;

    @Schema(description = "创建者头像")
    private String creatorAvatar;

    @Schema(description = "更新者用户名")
    private String updatorUsername;

    @Schema(description = "文档名称")
    private String documentName;

    @Schema(description = "知识库名称")
    private String datasetName;
}