package com.yxboot.llm.chat.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.yxboot.llm.chat.ChatModel;
import com.yxboot.llm.chat.StreamingChatModel;

/**
 * Represents a chat message.
 * Used together with {@link ChatModel} and {@link StreamingChatModel}.
 *
 * @see SystemMessage
 * @see UserMessage
 * @see AiMessage
 * @see ToolExecutionResultMessage
 * @see CustomMessage
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "role")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AiMessage.class, name = "assistant"),
        @JsonSubTypes.Type(value = UserMessage.class, name = "user"),
        @JsonSubTypes.Type(value = SystemMessage.class, name = "system"),
        @JsonSubTypes.Type(value = ToolExecutionResultMessage.class, name = "tool_execution_result"),
        @JsonSubTypes.Type(value = CustomMessage.class, name = "custom")
})
public interface Message {

    String getContent();

    /**
     * The type of the message.
     *
     * @return the type of the message
     */
    MessageType type();
}
