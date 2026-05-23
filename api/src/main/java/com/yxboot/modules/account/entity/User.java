package com.yxboot.modules.account.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;
import com.yxboot.modules.account.enums.UserStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户表实体类
 *
 * @author Boya
 */
@Data
@Table(value = "user", onInsert = MyFlexListener.class, onUpdate = MyFlexListener.class)
@Schema(description = "用户信息")
public class User {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "头像ID")
    private Long avatarId;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "状态(pending:待处理, uninitialized:未初始化, active:活跃, banned:已禁止, closed:已关闭)")
    private UserStatus status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
