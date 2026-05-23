package com.yxboot.config.mybatisflex;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

import com.mybatisflex.core.audit.AuditManager;

import jakarta.annotation.PostConstruct;

/**
 * MyBatis-Flex 配置
 *
 * @author Boya
 */
@Configuration
@MapperScan("com.yxboot.modules.**.mapper")
public class MyBatisFlexConfig {

    @PostConstruct
    public void init() {
        AuditManager.setAuditEnable(true);
        AuditManager.setMessageCollector(auditMessage -> {
            System.out.println("SQL: " + auditMessage.getFullSql());
            System.out.println("执行时间: " + auditMessage.getElapsedTime() + " ms");
        });
    }
}
