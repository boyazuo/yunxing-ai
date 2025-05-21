package com.yxboot.llm.document.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.yxboot.llm.document.Document;
import com.yxboot.llm.document.DocumentChunk;

/**
 * 抽象文档分割器
 */
public abstract class AbstractSplitter implements DocumentSplitter {

    @Override
    public List<DocumentChunk> split(Document document) {
        List<DocumentChunk> chunks = split(document.getContent());

        // 将文档的元数据添加到每个块中
        Map<String, Object> metadata = document.getMetadata();
        chunks.forEach(chunk -> {
            metadata.forEach((key, value) -> {
                if (!chunk.getMetadata().containsKey(key)) {
                    chunk.getMetadata().put(key, value);
                }
            });
        });

        return chunks;
    }

    @Override
    public List<DocumentChunk> split(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> textChunks = splitText(text);
        List<DocumentChunk> documentChunks = new ArrayList<>();

        for (String textChunk : textChunks) {
            if (textChunk.trim().isEmpty()) {
                continue;
            }

            DocumentChunk chunk = DocumentChunk.builder()
                    .id(generateChunkId())
                    .content(textChunk)
                    .build();

            documentChunks.add(chunk);
        }

        return documentChunks;
    }

    /**
     * 分割文本内容
     *
     * @param text 文本内容
     * @return 分割后的文本列表
     */
    protected abstract List<String> splitText(String text);

    /**
     * 生成块ID
     *
     * @return 块ID
     */
    protected String generateChunkId() {
        return UUID.randomUUID().toString();
    }
}