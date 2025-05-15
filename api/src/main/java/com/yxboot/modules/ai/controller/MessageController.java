package com.yxboot.modules.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.modules.ai.dto.MessageDTO;
import com.yxboot.modules.ai.entity.Conversation;
import com.yxboot.modules.ai.entity.Message;
import com.yxboot.modules.ai.enums.MessageStatus;
import com.yxboot.modules.ai.service.ConversationService;
import com.yxboot.modules.ai.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 消息控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/messages")
@Tag(name = "消息API", description = "消息管理相关接口")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ConversationService conversationService;

    @GetMapping("/conversation/{conversationId}")
    @Operation(summary = "获取会话消息", description = "获取指定会话下的所有消息")
    public Result<List<MessageDTO>> getConversationMessages(@PathVariable Long conversationId) {
        // 验证会话是否存在
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return Result.error(ResultCode.NOT_FOUND, "会话不存在");
        }

        // 获取会话消息
        List<MessageDTO> messages = messageService.getConversationMessages(conversationId);
        return Result.success("查询成功", messages);
    }

    @PostMapping
    @Operation(summary = "创建消息", description = "创建新的消息")
    public Result<MessageDTO> createMessage(@RequestBody MessageRequest request) {
        // 参数验证
        if (request.getUserId() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "用户ID不能为空");
        }
        if (request.getAppId() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "应用ID不能为空");
        }
        if (request.getConversationId() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "会话ID不能为空");
        }
        if (request.getQuestion() == null || request.getQuestion().isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "问题不能为空");
        }

        // 验证会话是否存在
        Conversation conversation = conversationService.getById(request.getConversationId());
        if (conversation == null) {
            return Result.error(ResultCode.NOT_FOUND, "会话不存在");
        }

        // 创建消息
        Message message = messageService.createMessage(
                request.getUserId(),
                request.getAppId(),
                request.getConversationId(),
                request.getQuestion());

        return Result.success("消息创建成功", MessageDTO.fromMessage(message));
    }

    @PutMapping("/{messageId}/answer")
    @Operation(summary = "更新消息回复", description = "更新消息的回复内容和状态")
    public Result<MessageDTO> updateMessageAnswer(
            @PathVariable Long messageId,
            @RequestBody AnswerUpdateRequest request) {
        // 验证消息是否存在
        Message message = messageService.getById(messageId);
        if (message == null) {
            return Result.error(ResultCode.NOT_FOUND, "消息不存在");
        }

        // 参数验证
        if (request.getAnswer() == null || request.getAnswer().isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "回复内容不能为空");
        }
        if (request.getStatus() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "状态不能为空");
        }

        // 更新消息回复
        boolean updated = messageService.updateMessageAnswer(messageId, request.getAnswer(), request.getStatus());
        if (!updated) {
            return Result.error(ResultCode.FAIL, "消息回复更新失败");
        }

        return Result.success("消息回复更新成功", MessageDTO.fromMessage(messageService.getById(messageId)));
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "删除消息", description = "删除指定消息")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        // 验证消息是否存在
        Message message = messageService.getById(messageId);
        if (message == null) {
            return Result.error(ResultCode.NOT_FOUND, "消息不存在");
        }

        // 删除消息
        boolean removed = messageService.removeById(messageId);
        if (!removed) {
            return Result.error(ResultCode.FAIL, "消息删除失败");
        }

        return Result.success("消息已删除");
    }

    @Data
    public static class MessageRequest {
        private Long userId;
        private Long appId;
        private Long conversationId;
        private String question;
    }

    @Data
    public static class AnswerUpdateRequest {
        private String answer;
        private MessageStatus status;
    }
}