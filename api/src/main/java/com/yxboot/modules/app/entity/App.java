package com.yxboot.modules.app.entity;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;
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
@Table(value = "app", onInsert = MyFlexListener.class, onUpdate = MyFlexListener.class)
@Schema(description = "应用信息")
public class App {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "所属租户ID")
    private Long tenantId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用介绍")
    private String intro;

    @Schema(description = "应用Logo")
    private String logo;

    @Schema(description = "应用Logo背景色")
    private String logoBackground;

    @Schema(description = "应用类型")
    private AppType type;

    @Schema(description = "状态")
    private AppStatus status;

    @Schema(description = "创建者ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者ID")
    private Long updatorId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
