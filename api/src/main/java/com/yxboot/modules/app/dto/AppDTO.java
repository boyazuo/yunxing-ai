package com.yxboot.modules.app.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yxboot.modules.app.entity.App;
import com.yxboot.modules.app.enums.AppStatus;
import com.yxboot.modules.app.enums.AppType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用信息DTO，包含创建者和更新者的用户名
 * 
 * @author Boya
 */
@Data
@Schema(description = "应用信息DTO")
public class AppDTO {

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

    @Schema(description = "应用Logo背景色")
    private String logoBackground;

    @Schema(description = "应用类型")
    private AppType type;

    @Schema(description = "状态")
    private AppStatus status;

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
     * 从App实体转换为AppDTO
     */
    public static AppDTO fromApp(App app) {
        AppDTO dto = new AppDTO();
        dto.setAppId(app.getAppId());
        dto.setTenantId(app.getTenantId());
        dto.setAppName(app.getAppName());
        dto.setIntro(app.getIntro());
        dto.setLogo(app.getLogo());
        dto.setLogoBackground(app.getLogoBackground());
        dto.setType(app.getType());
        dto.setStatus(app.getStatus());
        dto.setCreatorId(app.getCreatorId());
        dto.setCreateTime(app.getCreateTime());
        dto.setUpdatorId(app.getUpdatorId());
        dto.setUpdateTime(app.getUpdateTime());
        return dto;
    }
}