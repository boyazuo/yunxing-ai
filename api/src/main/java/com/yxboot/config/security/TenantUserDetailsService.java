package com.yxboot.config.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.yxboot.modules.account.entity.User;
import com.yxboot.modules.account.enums.UserStatus;
import com.yxboot.modules.account.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 实现UserDetailsService接口，用于加载用户信息
 *
 * @author Boya
 */
@Slf4j
@Component(value = "tenantUserDetailsService")
@RequiredArgsConstructor
public class TenantUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 通过邮箱查询用户
        User user = userService.getUserByEmail(email);

        if (user == null) {
            log.warn("用户邮箱不存在: {}", email);
            throw new UsernameNotFoundException("用户邮箱不存在");
        }

        // 检查用户状态
        if (user.getStatus() == UserStatus.BANNED || user.getStatus() == UserStatus.CLOSED) {
            log.warn("用户状态异常: {}, 状态: {}", email, user.getStatus());
            throw new UsernameNotFoundException("用户状态异常");
        }

        // 创建权限列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 返回UserDetails对象
        return new SecurityUser(user.getUserId(), email, user.getPassword(), authorities);
    }
}
