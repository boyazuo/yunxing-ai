package com.yxboot.ai.support;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import com.yxboot.modules.ai.dto.ChatMessageDTO;

/**
 * 将 API 对话消息 DTO 转换为 Spring AI Message。
 */
public final class SpringAiMessageConverter {

    private SpringAiMessageConverter() {}

    public static List<Message> toSpringAiMessages(List<ChatMessageDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }
        List<Message> result = new ArrayList<>(dtos.size());
        for (ChatMessageDTO dto : dtos) {
            Message converted = toSpringAiMessage(dto);
            if (converted != null) {
                result.add(converted);
            }
        }
        return result;
    }

    public static Message toSpringAiMessage(ChatMessageDTO dto) {
        if (dto == null || dto.getContent() == null) {
            return null;
        }
        String role = dto.getRole() != null ? dto.getRole().toLowerCase() : "user";
        return switch (role) {
            case "system" -> new SystemMessage(dto.getContent());
            case "assistant" -> new AssistantMessage(dto.getContent());
            default -> new UserMessage(dto.getContent());
        };
    }
}
