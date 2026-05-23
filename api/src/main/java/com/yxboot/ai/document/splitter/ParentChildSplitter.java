package com.yxboot.ai.document.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.yxboot.ai.document.Document;
import com.yxboot.ai.document.DocumentSegment;
import com.yxboot.modules.dataset.enums.SegmentType;

/**
 * 父子分块策略：先切父块，再对每个父块切子块。
 */
@Component
public class ParentChildSplitter implements DocumentSplitter {

    private static final int DEFAULT_PARENT_CHUNK_SIZE = 1200;
    private static final int DEFAULT_CHILD_CHUNK_SIZE = 300;
    private static final int DEFAULT_CHILD_OVERLAP_SIZE = 50;

    public List<DocumentSegment> split(Document document, int parentChunkSize, int childChunkSize,
            int childOverlapSize) {
        if (document == null || document.getContent() == null || document.getContent().trim().isEmpty()) {
            return List.of();
        }

        CharacterSplitter parentSplitter = new CharacterSplitter(parentChunkSize, 0);
        CharacterSplitter childSplitter = new CharacterSplitter(childChunkSize, childOverlapSize);

        List<DocumentSegment> parentChunks = parentSplitter.split(Document.of(document.getContent()));
        Map<String, Object> docMetadata = document.getMetadata();
        List<DocumentSegment> result = new ArrayList<>();

        for (DocumentSegment parentChunk : parentChunks) {
            String parentId = UUID.randomUUID().toString();
            DocumentSegment parent = DocumentSegment.builder()
                    .id(parentId)
                    .title(generateParentTitle(parentChunk.getContent()))
                    .content(parentChunk.getContent())
                    .segmentType(SegmentType.PARENT)
                    .build();
            copyMetadata(docMetadata, parent);
            result.add(parent);

            List<DocumentSegment> children = childSplitter.split(Document.of(parentChunk.getContent()));
            for (DocumentSegment child : children) {
                DocumentSegment childSegment = DocumentSegment.builder()
                        .id(UUID.randomUUID().toString())
                        .title(child.getTitle())
                        .content(child.getContent())
                        .segmentType(SegmentType.CHILD)
                        .parentId(parentId)
                        .build();
                copyMetadata(docMetadata, childSegment);
                result.add(childSegment);
            }
        }

        return result;
    }

    public List<DocumentSegment> splitWithDefaults(Document document) {
        return split(document, DEFAULT_PARENT_CHUNK_SIZE, DEFAULT_CHILD_CHUNK_SIZE, DEFAULT_CHILD_OVERLAP_SIZE);
    }

    @Override
    public List<DocumentSegment> split(Document document) {
        return splitWithDefaults(document);
    }

    @Override
    public List<DocumentSegment> split(String text) {
        return split(Document.of(text));
    }

    private void copyMetadata(Map<String, Object> source, DocumentSegment target) {
        if (source == null) {
            return;
        }
        source.forEach((key, value) -> {
            if (!target.getMetadata().containsKey(key)) {
                target.getMetadata().put(key, value);
            }
        });
    }

    private String generateParentTitle(String content) {
        if (content == null || content.isBlank()) {
            return "分段";
        }
        String trimmed = content.replaceAll("\\s+", " ").trim();
        if (trimmed.length() <= 30) {
            return trimmed;
        }
        return trimmed.substring(0, 30);
    }
}
