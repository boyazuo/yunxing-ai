package com.yxboot.config.mybatisplus.handler;

import java.time.LocalDateTime;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.yxboot.util.SecurityUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段填充审计
 * 
 * @author Boya
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("insertFill: {}", metaObject);
        this.strictInsertFill(metaObject, "creatorId", Long.class, getCurrentUserId());
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatorId", Long.class, getCurrentUserId());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        this.strictInsertFill(metaObject, "inviteTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatorId", Long.class, getCurrentUserId());
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    private Long getCurrentUserId() {
        return SecurityUtil.getCurrentUserId();
    }
}
