package com.yxboot.llm.chat.message;

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
public interface Message {

    /**
     * The type of the message.
     *
     * @return the type of the message
     */
    MessageType type();
}
