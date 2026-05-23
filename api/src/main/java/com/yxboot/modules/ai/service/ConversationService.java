package com.yxboot.modules.ai.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.ai.dto.ConversationDTO;
import com.yxboot.modules.ai.entity.Conversation;
import com.yxboot.modules.ai.mapper.ConversationMapper;

import lombok.RequiredArgsConstructor;

import static com.yxboot.modules.ai.entity.table.ConversationTableDef.CONVERSATION;

/**
 * 会话服务实现类
 */
@Service
@RequiredArgsConstructor
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {

    public Conversation createConversation(Long userId, Long appId, String title) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setAppId(appId);
        conversation.setTitle(title);
        save(conversation);
        return conversation;
    }

    public Page<ConversationDTO> getUserAppConversations(Long userId, Long appId, Long current, Long size) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(CONVERSATION.USER_ID.eq(userId));
        wrapper.where(CONVERSATION.APP_ID.eq(appId));
        wrapper.orderBy(CONVERSATION.UPDATE_TIME, false);

        Page<Conversation> page = page(Page.of(current, size), wrapper);

        Page<ConversationDTO> dtoPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        dtoPage.setRecords(page.getRecords().stream()
                .map(ConversationDTO::fromConversation)
                .collect(Collectors.toList()));

        return dtoPage;
    }

    public boolean updateConversationTitle(Long conversationId, String title) {
        Conversation conversation = getById(conversationId);
        if (conversation == null) {
            return false;
        }
        conversation.setTitle(title);
        return updateById(conversation);
    }
}
