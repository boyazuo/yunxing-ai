package com.yxboot.modules.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.modules.ai.dto.ConversationDTO;
import com.yxboot.modules.ai.entity.Conversation;
import com.yxboot.modules.ai.service.ConversationService;
import com.yxboot.modules.ai.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 会话控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/conversations")
@Tag(name = "会话API", description = "会话管理相关接口")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "获取会话列表", description = "获取用户的会话列表")
    public Result<List<ConversationDTO>> getConversations(
            @RequestParam @Parameter(description = "租户ID") Long tenantId,
            @RequestParam @Parameter(description = "用户ID") Long userId,
            @RequestParam @Parameter(description = "应用ID") Long appId) {
        List<ConversationDTO> conversations = conversationService.getUserAppConversations(tenantId, userId, appId);
        return Result.success("查询成功", conversations);
    }

    @PostMapping
    @Operation(summary = "创建会话", description = "创建新的会话")
    public Result<ConversationDTO> createConversation(@RequestBody ConversationRequest request) {
        // 参数验证
        if (request.getTenantId() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "租户ID不能为空");
        }
        if (request.getUserId() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "用户ID不能为空");
        }
        if (request.getAppId() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "应用ID不能为空");
        }

        // 创建会话
        Conversation conversation = conversationService.createConversation(
                request.getTenantId(),
                request.getUserId(),
                request.getAppId(),
                request.getTitle());

        return Result.success("会话创建成功", ConversationDTO.fromConversation(conversation));
    }

    @PutMapping("/{conversationId}/title")
    @Operation(summary = "更新会话标题", description = "更新会话标题")
    public Result<ConversationDTO> updateConversationTitle(
            @PathVariable Long conversationId,
            @RequestBody TitleUpdateRequest request) {
        // 验证会话是否存在
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return Result.error(ResultCode.NOT_FOUND, "会话不存在");
        }

        // 参数验证
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "标题不能为空");
        }

        // 更新会话标题
        boolean updated = conversationService.updateConversationTitle(conversationId, request.getTitle());
        if (!updated) {
            return Result.error(ResultCode.FAIL, "会话标题更新失败");
        }

        return Result.success("会话标题更新成功",
                ConversationDTO.fromConversation(conversationService.getById(conversationId)));
    }

    @DeleteMapping("/{conversationId}")
    @Operation(summary = "删除会话", description = "删除指定会话及其所有消息")
    public Result<Void> deleteConversation(@PathVariable Long conversationId) {
        // 验证会话是否存在
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return Result.error(ResultCode.NOT_FOUND, "会话不存在");
        }

        // 先删除会话下的所有消息
        messageService.deleteConversationMessages(conversationId);

        // 删除会话
        boolean removed = conversationService.removeById(conversationId);
        if (!removed) {
            return Result.error(ResultCode.FAIL, "会话删除失败");
        }

        return Result.success("会话已删除");
    }

    @Data
    public static class ConversationRequest {
        private Long tenantId;
        private Long userId;
        private Long appId;
        private String title;
    }

    @Data
    public static class TitleUpdateRequest {
        private String title;
    }
}