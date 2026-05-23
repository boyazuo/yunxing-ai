package com.yxboot.modules.ai.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.ai.dto.MessageDTO;
import com.yxboot.modules.ai.entity.Message;
import com.yxboot.modules.ai.enums.MessageStatus;
import com.yxboot.modules.ai.mapper.MessageMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.yxboot.modules.ai.entity.table.MessageTableDef.MESSAGE;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    public Message createMessage(Long userId, Long appId, Long conversationId, String question) {
        Message message = new Message();
        message.setUserId(userId);
        message.setAppId(appId);
        message.setConversationId(conversationId);
        message.setQuestion(question);
        message.setStatus(MessageStatus.PENDING);
        save(message);
        return message;
    }

    public boolean updateMessageAnswer(Long messageId, String answer, MessageStatus status) {
        Message message = getById(messageId);
        if (message == null) {
            return false;
        }
        message.setAnswer(answer);
        message.setStatus(status);
        return updateById(message);
    }

    public List<MessageDTO> getConversationMessages(Long conversationId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(MESSAGE.CONVERSATION_ID.eq(conversationId));
        wrapper.orderBy(MESSAGE.CREATE_TIME, true);
        List<Message> messages = list(wrapper);
        return messages.stream()
                .map(MessageDTO::fromMessage)
                .collect(Collectors.toList());
    }

    public boolean deleteConversationMessages(Long conversationId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(MESSAGE.CONVERSATION_ID.eq(conversationId));
        remove(wrapper);
        log.info("删除会话消息完成, conversationId: {}", conversationId);
        return true;
    }
}
