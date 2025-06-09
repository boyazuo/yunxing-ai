package com.yxboot.llm.document.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * 基于字符长度的文档分割器
 */
@Component
public class CharacterSplitter extends AbstractSplitter {

    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n");
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("(?<=[.!?。！？])(\\s+)");

    /**
     * 默认块的最大字符数
     */
    private int maxChunkSize = 500;

    /**
     * 默认块的重叠字符数
     */
    private int overlapSize = 100;

    /**
     * 创建一个默认配置的字符长度分割器
     */
    public CharacterSplitter() {
    }

    /**
     * 创建一个自定义配置的字符长度分割器
     * 
     * @param maxChunkSize 块的最大字符数
     * @param overlapSize  块的重叠字符数
     */
    public CharacterSplitter(int maxChunkSize, int overlapSize) {
        if (maxChunkSize <= 0) {
            throw new IllegalArgumentException("最大块大小必须为正数");
        }
        if (overlapSize < 0) {
            throw new IllegalArgumentException("重叠大小不能为负数");
        }
        if (overlapSize >= maxChunkSize) {
            throw new IllegalArgumentException("重叠大小必须小于最大块大小");
        }

        this.maxChunkSize = maxChunkSize;
        this.overlapSize = overlapSize;
    }

    @Override
    protected List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();
        // 首先按段落分割
        String[] paragraphs = PARAGRAPH_PATTERN.split(text);

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            // 如果当前段落的长度超过最大块大小，需要进一步分割
            if (paragraph.length() > maxChunkSize) {
                // 如果当前块不为空，先添加到结果中
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                }

                // 按句子分割段落
                String[] sentences = SENTENCE_PATTERN.split(paragraph);
                StringBuilder sentenceChunk = new StringBuilder();

                for (String sentence : sentences) {
                    // 如果单个句子的长度超过块大小，直接按块大小切分
                    if (sentence.length() > maxChunkSize) {
                        // 处理之前积累的句子
                        if (sentenceChunk.length() > 0) {
                            chunks.add(sentenceChunk.toString());
                            sentenceChunk = new StringBuilder();
                        }

                        // 直接切分句子
                        int start = 0;
                        while (start < sentence.length()) {
                            int end = Math.min(start + maxChunkSize, sentence.length());
                            String chunk = sentence.substring(start, end);
                            chunks.add(chunk);
                            // 处理重叠
                            start = end - overlapSize;
                        }
                    } else {
                        // 如果添加当前句子后超过最大块大小，则创建新块
                        if (sentenceChunk.length() + sentence.length() > maxChunkSize) {
                            chunks.add(sentenceChunk.toString());

                            // 如果设置了重叠，需要将最后部分重叠到新块中
                            if (overlapSize > 0 && sentenceChunk.length() > overlapSize) {
                                int overlapStart = sentenceChunk.length() - overlapSize;
                                sentenceChunk = new StringBuilder(sentenceChunk.substring(overlapStart));
                            } else {
                                sentenceChunk = new StringBuilder();
                            }
                        }

                        // 添加当前句子到块中
                        if (sentenceChunk.length() > 0) {
                            sentenceChunk.append(" ");
                        }
                        sentenceChunk.append(sentence);
                    }
                }

                // 处理最后一个句子块
                if (sentenceChunk.length() > 0) {
                    chunks.add(sentenceChunk.toString());
                }
            } else {
                // 如果添加当前段落后超过最大块大小，则创建新块
                if (currentChunk.length() + paragraph.length() > maxChunkSize) {
                    chunks.add(currentChunk.toString());

                    // 如果设置了重叠，需要将最后部分重叠到新块中
                    if (overlapSize > 0 && currentChunk.length() > overlapSize) {
                        int overlapStart = currentChunk.length() - overlapSize;
                        currentChunk = new StringBuilder(currentChunk.substring(overlapStart));
                    } else {
                        currentChunk = new StringBuilder();
                    }
                }

                // 添加当前段落到块中
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            }
        }

        // 处理最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    /**
     * 设置最大块大小
     * 
     * @param maxChunkSize 最大块大小
     * @return 当前分割器实例
     */
    public CharacterSplitter setMaxChunkSize(int maxChunkSize) {
        if (maxChunkSize <= 0) {
            throw new IllegalArgumentException("最大块大小必须为正数");
        }
        if (maxChunkSize <= overlapSize) {
            throw new IllegalArgumentException("最大块大小必须大于重叠大小");
        }

        this.maxChunkSize = maxChunkSize;
        return this;
    }

    /**
     * 设置重叠大小
     * 
     * @param overlapSize 重叠大小
     * @return 当前分割器实例
     */
    public CharacterSplitter setOverlapSize(int overlapSize) {
        if (overlapSize < 0) {
            throw new IllegalArgumentException("重叠大小不能为负数");
        }
        if (overlapSize >= maxChunkSize) {
            throw new IllegalArgumentException("重叠大小必须小于最大块大小");
        }

        this.overlapSize = overlapSize;
        return this;
    }
}