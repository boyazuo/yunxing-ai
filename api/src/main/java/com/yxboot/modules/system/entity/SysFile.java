package com.yxboot.modules.system.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.yxboot.config.mybatisflex.MyFlexListener;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 附件表
 *
 * @author Boya
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(value = "sys_file", onInsert = MyFlexListener.class)
@Schema(name = "SysFile", description = "附件表")
public class SysFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id(value = "file_id", keyType = KeyType.Auto)
    @Schema(description = "附件编号")
    private Long fileId;

    @Schema(description = "原始文件名称")
    private String originName;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件路径")
    private String path;

    @Schema(description = "文件URL")
    private String url;

    @Schema(description = "文件hash值")
    private String hash;

    @Schema(description = "ContentType")
    private String contentType;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "创建人")
    private Long createUserId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "状态")
    private Integer status;
}
