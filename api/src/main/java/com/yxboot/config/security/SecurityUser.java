package com.yxboot.config.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * 封装 SecurityUser
 * 
 * @author Boya
 */
public class SecurityUser extends User {

    private Long userId;

    public SecurityUser(Long userId, String username, String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
