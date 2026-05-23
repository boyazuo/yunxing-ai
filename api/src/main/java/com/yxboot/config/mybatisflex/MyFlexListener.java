package com.yxboot.config.mybatisflex;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.yxboot.util.SecurityUtil;

/**
 * Insert/Update 审计字段自动填充
 *
 * @author Boya
 */
public class MyFlexListener implements InsertListener, UpdateListener {

    @Override
    public void onInsert(Object entity) {
        LocalDateTime now = LocalDateTime.now();
        setFieldValue(entity, "createTime", now);
        setFieldValue(entity, "updateTime", now);
        setFieldValue(entity, "inviteTime", now);

        Long userId = SecurityUtil.getCurrentUserId();
        if (userId != null) {
            setFieldValue(entity, "creatorId", userId);
            setFieldValue(entity, "updatorId", userId);
            setFieldValue(entity, "createUserId", userId);
        }
    }

    @Override
    public void onUpdate(Object entity) {
        setFieldValue(entity, "updateTime", LocalDateTime.now());

        Long userId = SecurityUtil.getCurrentUserId();
        if (userId != null) {
            setFieldValue(entity, "updatorId", userId);
        }
    }

    private void setFieldValue(Object entity, String fieldName, Object value) {
        try {
            Field field = findField(entity.getClass(), fieldName);
            if (field == null) {
                return;
            }
            field.setAccessible(true);
            if (field.get(entity) == null) {
                field.set(entity, value);
            }
        } catch (IllegalAccessException ignored) {
            // 字段不存在或无法访问时静默忽略
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
