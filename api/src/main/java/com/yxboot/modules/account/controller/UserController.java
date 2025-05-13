package com.yxboot.modules.account.controller;

import com.yxboot.common.api.Result;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.account.entity.User;
import com.yxboot.modules.account.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/user")
@Tag(name = "用户管理API", description = "用户管理相关的接口")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 更新用户信息
     */
    @PutMapping
    @Operation(summary = "更新用户信息", description = "更新指定租户的基本信息")
    public Result<User> updateTenant(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody User user) {

        // 没有 用户ID 更新当前登录用户
        if (user.getUserId() == null) {
            user.setUserId(securityUser.getUserId());
        }

        // TODO 用户名是可以重复的？？？？
        userService.updateById(user);
        return Result.success("更新用户信息成功", user);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    public Result<User> getUserInfo(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long userId
    ) {
        User user;
        if (userId == null) {
            user = userService.getById(securityUser.getUserId());
        } else {
            user = userService.getById(userId);
        }
        return Result.success("获取用户信息成功", user);
    }
}
