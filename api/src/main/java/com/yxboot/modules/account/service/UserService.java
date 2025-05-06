package com.yxboot.modules.account.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.account.entity.User;
import com.yxboot.modules.account.enums.UserStatus;
import com.yxboot.modules.account.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * 用户服务实现类
 * 
 * @author Boya
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 创建用户
     * 
     * @param username 用户名
     * @param email    邮箱
     * @param password 密码
     * @param status   状态
     */
    public Long createUser(String username, String email, String password, UserStatus status) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setStatus(status);
        save(user);
        return user.getUserId();
    }

    /**
     * 根据邮箱获取用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return getOne(queryWrapper);
    }
}