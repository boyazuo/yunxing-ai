package com.yxboot.modules.ai.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @param appId    应用ID
     * @param title    会话标题
     * @return 会话
     */
    public Conversation createConversation(Long tenantId, Long userId, Long appId, String title) {
        Conversation conversation = new Conversation();
        conversation.setTenantId(tenantId);
        conversation.setUserId(userId);
        conversation.setAppId(appId);
        conversation.setTitle(title);
        save(conversation);
        return conversation;
    }

    /**
     * 获取用户在指定应用下的会话列表
     * 
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @param appId    应用ID
     * @return 会话列表
     */
    public List<ConversationDTO> getUserAppConversations(Long tenantId, Long userId, Long appId) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getTenantId, tenantId)
                .eq(Conversation::getUserId, userId)
                .eq(Conversation::getAppId, appId)
                .orderByDesc(Conversation::getUpdateTime);
        List<Conversation> conversations = list(queryWrapper);
        return conversations.stream()
                .map(ConversationDTO::fromConversation)
                .collect(Collectors.toList());
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