package com.yxboot.llm.chat.prompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.message.MessageBuilder;
import com.yxboot.llm.chat.message.SystemMessage;
import com.yxboot.llm.chat.message.UserMessage;

/**
 * 提示词模板
 * 支持创建带变量的提示词模板，用于动态生成提示词
 * 
 * @author Boya
 */
public class PromptTemplate {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*\\}\\}");

    private final List<Message> templateMessages;
    private final Map<String, Object> defaultVariables;
    private final ChatOptions defaultOptions;

    /**
     * 创建一个空的提示词模板
     */
    public PromptTemplate() {
        this(MessageBuilder.create().build(), new HashMap<>(), new ChatOptions());
    }

    /**
     * 从单个系统消息创建提示词模板
     * 
     * @param systemPrompt 系统提示词
     */
    public PromptTemplate(String systemPrompt) {
        this(MessageBuilder.create()
                .system(systemPrompt)
                .build(),
                new HashMap<>(),
                new ChatOptions());
    }

    /**
     * 从系统消息和用户消息创建提示词模板
     * 
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     */
    public PromptTemplate(String systemPrompt, String userPrompt) {
        this(MessageBuilder.create()
                .system(systemPrompt)
                .user(userPrompt)
                .build(),
                new HashMap<>(),
                new ChatOptions());
    }

    /**
     * 从消息列表创建提示词模板
     * 
     * @param templateMessages 模板消息列表
     */
    public PromptTemplate(List<Message> templateMessages) {
        this(templateMessages, new HashMap<>(), new ChatOptions());
    }

    /**
     * 从消息列表和默认变量创建提示词模板
     * 
     * @param templateMessages 模板消息列表
     * @param defaultVariables 默认变量
     */
    public PromptTemplate(List<Message> templateMessages, Map<String, Object> defaultVariables) {
        this(templateMessages, defaultVariables, new ChatOptions());
    }

    /**
     * 从消息列表、默认变量和默认选项创建提示词模板
     * 
     * @param templateMessages 模板消息列表
     * @param defaultVariables 默认变量
     * @param defaultOptions   默认选项
     */
    public PromptTemplate(List<Message> templateMessages, Map<String, Object> defaultVariables,
            ChatOptions defaultOptions) {
        this.templateMessages = templateMessages;
        this.defaultVariables = defaultVariables;
        this.defaultOptions = defaultOptions;
    }

    /**
     * 设置默认变量
     * 
     * @param name  变量名
     * @param value 变量值
     * @return 提示词模板
     */
    public PromptTemplate withDefaultVariable(String name, Object value) {
        Map<String, Object> newVariables = new HashMap<>(this.defaultVariables);
        newVariables.put(name, value);
        return new PromptTemplate(this.templateMessages, newVariables, this.defaultOptions);
    }

    /**
     * 设置默认选项
     * 
     * @param name  选项名
     * @param value 选项值
     * @return 提示词模板
     */
    public PromptTemplate withDefaultOption(String name, Object value) {
        ChatOptions newOptions = new ChatOptions();
        // 复制已有参数
        defaultOptions.toMap().forEach(newOptions::withParameter);
        // 添加新参数
        newOptions.withParameter(name, value);
        return new PromptTemplate(this.templateMessages, this.defaultVariables, newOptions);
    }

    /**
     * 格式化模板，生成提示词对象
     * 
     * @return 提示词对象
     */
    public Prompt format() {
        return format(new HashMap<>());
    }

    /**
     * 格式化模板，生成提示词对象
     * 
     * @param variables 变量映射
     * @return 提示词对象
     */
    public Prompt format(Map<String, Object> variables) {
        Map<String, Object> allVariables = new HashMap<>(this.defaultVariables);
        allVariables.putAll(variables);

        MessageBuilder messageBuilder = MessageBuilder.create();

        for (Message templateMessage : templateMessages) {
            if (templateMessage instanceof SystemMessage) {
                SystemMessage systemMessage = (SystemMessage) templateMessage;
                String formattedContent = formatText(systemMessage.getContent(), allVariables);
                messageBuilder.system(formattedContent);
            } else if (templateMessage instanceof UserMessage) {
                UserMessage userMessage = (UserMessage) templateMessage;
                String formattedContent = formatText(userMessage.getContent(), allVariables);
                messageBuilder.user(formattedContent);
            } else {
                messageBuilder.add(templateMessage);
            }
        }

        return new Prompt(messageBuilder.build(), this.defaultOptions);
    }

    /**
     * 格式化文本，替换变量
     * 
     * @param text      文本模板
     * @param variables 变量映射
     * @return 格式化后的文本
     */
    private String formatText(String text, Map<String, Object> variables) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            String replacement = (value != null) ? value.toString() : "null";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 创建一个系统提示模板构建器
     * 
     * @param systemPrompt 系统提示内容
     * @return 模板构建器
     */
    public static Builder system(String systemPrompt) {
        return new Builder().system(systemPrompt);
    }

    /**
     * 创建一个用户提示模板构建器
     * 
     * @param userPrompt 用户提示内容
     * @return 模板构建器
     */
    public static Builder user(String userPrompt) {
        return new Builder().user(userPrompt);
    }

    /**
     * 提示词模板构建器
     */
    public static class Builder {
        private final MessageBuilder messageBuilder = MessageBuilder.create();
        private final Map<String, Object> defaultVariables = new HashMap<>();
        private final ChatOptions defaultOptions = new ChatOptions();

        /**
         * 添加系统消息模板
         * 
         * @param content 系统消息内容
         * @return 构建器
         */
        public Builder system(String content) {
            messageBuilder.system(content);
            return this;
        }

        /**
         * 添加用户消息模板
         * 
         * @param content 用户消息内容
         * @return 构建器
         */
        public Builder user(String content) {
            messageBuilder.user(content);
            return this;
        }

        /**
         * 添加助手消息模板
         * 
         * @param content 助手消息内容
         * @return 构建器
         */
        public Builder assistant(String content) {
            messageBuilder.assistant(content);
            return this;
        }

        /**
         * 设置默认变量
         * 
         * @param name  变量名
         * @param value 变量值
         * @return 构建器
         */
        public Builder variable(String name, Object value) {
            defaultVariables.put(name, value);
            return this;
        }

        /**
         * 设置默认选项
         * 
         * @param name  选项名
         * @param value 选项值
         * @return 构建器
         */
        public Builder option(String name, Object value) {
            defaultOptions.withParameter(name, value);
            return this;
        }

        /**
         * 构建提示词模板
         * 
         * @return 提示词模板
         */
        public PromptTemplate build() {
            return new PromptTemplate(messageBuilder.build(), defaultVariables, defaultOptions);
        }
    }
}