package com.yxboot.modules.app.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yxboot.modules.app.enums.AppStatus;
import com.yxboot.modules.app.enums.AppType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用表实体类
 * 
 * @author Boya
 */
@Data
@TableName("app")
@Schema(description = "应用信息")
public class App {

    @TableId(value = "app_id", type = IdType.ASSIGN_ID)
    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "所属租户ID")
    private String tenantId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用介绍")
    private String intro;

    @Schema(description = "应用Logo")
    private String logo;

    @Schema(description = "应用类型")
    private AppType type;

    @Schema(description = "状态")
    private AppStatus status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}