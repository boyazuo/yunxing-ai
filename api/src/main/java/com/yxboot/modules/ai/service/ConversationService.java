package com.yxboot.modules.ai.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.ai.dto.ConversationDTO;
import com.yxboot.modules.ai.entity.Conversation;
import com.yxboot.modules.ai.mapper.ConversationMapper;

import lombok.RequiredArgsConstructor;

/**
 * 会话服务实现类
 * 
 * @author Boya
 */
@Service
@RequiredArgsConstructor
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {

    /**
     * 创建会话
     * 
     * @param userId 用户ID
     * @param appId  应用ID
     * @param title  会话标题
     * @return 会话
     */
    public Conversation createConversation(Long userId, Long appId, String title) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setAppId(appId);
        conversation.setTitle(title);
        save(conversation);
        return conversation;
    }

    /**
     * 获取用户在指定应用下的会话列表
     * 
     * @param userId  用户ID
     * @param appId   应用ID
     * @param current 当前页
     * @param size    每页大小
     * @return 会话列表分页对象
     */
    public Page<ConversationDTO> getUserAppConversations(Long userId, Long appId, Long current, Long size) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getAppId, appId)
                .orderByDesc(Conversation::getUpdateTime);

        Page<Conversation> page = page(new Page<>(current, size), queryWrapper);

        Page<ConversationDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(ConversationDTO::fromConversation)
                .collect(Collectors.toList()));

        return dtoPage;
    }

    /**
     * 更新会话标题
     * 
     * @param conversationId 会话ID
     * @param title          会话标题
     * @return 是否成功
     */
    public boolean updateConversationTitle(Long conversationId, String title) {
        Conversation conversation = getById(conversationId);
        if (conversation == null) {
            return false;
        }
        conversation.setTitle(title);
        return updateById(conversation);
    }
}