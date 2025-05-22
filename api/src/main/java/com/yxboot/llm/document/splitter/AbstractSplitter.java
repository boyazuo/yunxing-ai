package com.yxboot.llm.document.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.yxboot.llm.document.Document;
import com.yxboot.llm.document.DocumentSegment;

/**
 * 抽象文档分割器
 */
public abstract class AbstractSplitter implements DocumentSplitter {

    @Override
    public List<DocumentSegment> split(Document document) {
        List<DocumentSegment> segments = split(document.getContent());

        // 将文档的元数据添加到每个块中
        Map<String, Object> metadata = document.getMetadata();
        segments.forEach(segment -> {
            metadata.forEach((key, value) -> {
                if (!segment.getMetadata().containsKey(key)) {
                    segment.getMetadata().put(key, value);
                }
            });
        });

        return segments;
    }

    @Override
    public List<DocumentSegment> split(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> textSegments = splitText(text);
        List<DocumentSegment> documentSegments = new ArrayList<>();

        for (String textSegment : textSegments) {
            if (textSegment.trim().isEmpty()) {
                continue;
            }

            DocumentSegment segment = DocumentSegment.builder()
                    .id(generateSegmentId())
                    .title(generateSegmentTitle())
                    .content(textSegment)
                    .build();

            documentSegments.add(segment);
        }

        return documentSegments;
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
    protected String generateSegmentId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成块标题
     *
     * @return 块标题
     */
    protected String generateSegmentTitle() {
        return "Segment " + generateSegmentId();
    }
}