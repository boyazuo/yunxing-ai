package com.yxboot.ai.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import com.yxboot.ai.document.DocumentSegment;
import com.yxboot.ai.document.splitter.ChapterSplitter;
import com.yxboot.ai.document.splitter.CharacterSplitter;
import com.yxboot.ai.document.splitter.ParentChildSplitter;
import com.yxboot.ai.document.splitter.SplitMode;
import lombok.RequiredArgsConstructor;

/**
 * 文档加载与分段服务（Spring AI Tika 加载 + Token/章节切分）。
 */
@Service
@RequiredArgsConstructor
public class AiDocumentProcessingService {

    private final ChapterSplitter chapterSplitter;
    private final CharacterSplitter characterSplitter;
    private final ParentChildSplitter parentChildSplitter;

    public List<DocumentSegment> loadAndSplitDocument(File file, SplitMode splitMode, Integer maxSegmentLength,
            Integer overlapLength) {
        return loadAndSplitDocument(file, splitMode, maxSegmentLength, overlapLength, null);
    }

    public List<DocumentSegment> loadAndSplitDocument(File file, SplitMode splitMode, Integer maxSegmentLength,
            Integer overlapLength, Integer parentChunkSize) {
        List<Document> rawDocs = new TikaDocumentReader(new FileSystemResource(file)).get();
        if (rawDocs.isEmpty()) {
            return List.of();
        }
        int maxLen = maxSegmentLength != null && maxSegmentLength > 0 ? maxSegmentLength : 500;
        int overlap = overlapLength != null && overlapLength >= 0 ? overlapLength : 100;
        int parentLen = parentChunkSize != null && parentChunkSize > 0 ? parentChunkSize : 1200;
        SplitMode mode = splitMode != null ? splitMode : SplitMode.CHARACTER_SPLITTER;

        return switch (mode) {
            case CHARACTER_SPLITTER -> splitByCharacter(rawDocs, maxLen, overlap);
            case CHAPTER_SPLITTER -> splitByChapter(rawDocs, maxLen, overlap);
            case PARENT_CHILD_SPLITTER -> splitByParentChild(rawDocs, parentLen, maxLen, overlap);
        };
    }

    private List<DocumentSegment> splitByParentChild(List<Document> rawDocs, int parentLen, int childLen, int overlap) {
        String merged = rawDocs.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
        com.yxboot.ai.document.Document legacyDoc = com.yxboot.ai.document.Document.of(merged);
        return parentChildSplitter.split(legacyDoc, parentLen, childLen, overlap);
    }

    private List<DocumentSegment> splitByCharacter(List<Document> rawDocs, int maxLen, int overlap) {
        String merged = rawDocs.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
        com.yxboot.ai.document.Document legacyDoc = com.yxboot.ai.document.Document.of(merged);
        CharacterSplitter splitter = new CharacterSplitter(maxLen, overlap);
        return splitter.split(legacyDoc);
    }

    private List<DocumentSegment> splitByChapter(List<Document> rawDocs, int maxLen, int overlap) {
        List<DocumentSegment> all = new ArrayList<>();
        for (Document raw : rawDocs) {
            com.yxboot.ai.document.Document legacyDoc = com.yxboot.ai.document.Document.of(raw.getText(), raw.getMetadata());
            List<DocumentSegment> segments = chapterSplitter.split(legacyDoc);
            all.addAll(segments);
        }
        if (all.isEmpty() && !rawDocs.isEmpty()) {
            return splitByCharacter(rawDocs, maxLen, overlap);
        }
        return all;
    }

    public List<DocumentSegment> loadAndSplitDocument(File file, SplitMode splitMode) {
        return loadAndSplitDocument(file, splitMode, 500, 100);
    }
}
