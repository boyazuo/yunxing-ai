package com.yxboot.modules.account.service;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.account.entity.User;
import com.yxboot.modules.account.enums.UserStatus;
import com.yxboot.modules.account.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

import static com.yxboot.modules.account.entity.table.UserTableDef.USER;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    public Long createUser(String username, String email, String password, UserStatus status) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setStatus(status);
        save(user);
        return user.getUserId();
    }

    public User getUserByEmail(String email) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(USER.EMAIL.eq(email));
        return getOne(wrapper);
    }
}
