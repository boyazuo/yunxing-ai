package com.yxboot.modules.ai.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.ai.dto.MessageDTO;
import com.yxboot.modules.ai.entity.Message;
import com.yxboot.modules.ai.enums.MessageStatus;
import com.yxboot.modules.ai.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息服务实现类
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    /**
     * 创建消息
     * 
     * @param userId 用户ID
     * @param appId 应用ID
     * @param conversationId 会话ID
     * @param question 问题
     * @return 消息
     */
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

    /**
     * 更新消息回复
     * 
     * @param messageId 消息ID
     * @param answer 回复
     * @param status 状态
     * @return 是否成功
     */
    public boolean updateMessageAnswer(Long messageId, String answer, MessageStatus status) {
        Message message = getById(messageId);
        if (message == null) {
            return false;
        }
        message.setAnswer(answer);
        message.setStatus(status);
        return updateById(message);
    }

    /**
     * 获取会话的消息列表
     * 
     * @param conversationId 会话ID
     * @return 消息列表
     */
    public List<MessageDTO> getConversationMessages(Long conversationId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreateTime);
        List<Message> messages = list(queryWrapper);
        return messages.stream()
                .map(MessageDTO::fromMessage)
                .collect(Collectors.toList());
    }

    /**
     * 删除会话的消息
     * 
     * @param conversationId 会话ID
     * @return 是否成功
     */
    public boolean deleteConversationMessages(Long conversationId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getConversationId, conversationId);
        remove(queryWrapper);
        log.info("删除会话消息完成, conversationId: {}", conversationId);
        return true;
    }
}
