package com.yxboot.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.yxboot.config.security.SecurityUser;

import lombok.extern.slf4j.Slf4j;

/**
 * 安全工具类
 * 提供安全上下文相关的工具方法
 * 
 * @author Boya
 */
@Slf4j
@Component
public class SecurityUtil {

    /**
     * 获取当前认证信息
     * 
     * @return 当前认证信息，如果未认证则返回null
     */
    private static Authentication getCurrentAuthentication() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null &&
                    authentication.isAuthenticated() &&
                    authentication.getName() != null &&
                    !(authentication instanceof AnonymousAuthenticationToken)) {
                return authentication;
            }
        } catch (Exception e) {
            log.warn("获取认证信息失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取当前用户ID
     * 
     * @return 当前用户ID，如果未认证则返回null
     */
    public static Long getCurrentUserId() {
        SecurityUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户信息
     * 
     * @return 当前用户信息，如果未认证则返回null
     */
    public static SecurityUser getCurrentUser() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof SecurityUser) {
                return (SecurityUser) principal;
            }
        }
        return null;
    }

    /**
     * 获取当前用户名
     * 
     * @return 当前用户名，如果未认证则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * 检查当前用户是否已认证
     * 
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication() != null;
    }
}