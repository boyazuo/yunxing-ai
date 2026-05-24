package com.yxboot.ai.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import com.yxboot.ai.document.DocumentSegment;
import com.yxboot.ai.document.loader.PdfDocumentLoader;
import com.yxboot.ai.document.splitter.ChapterSplitter;
import com.yxboot.ai.document.splitter.CharacterSplitter;
import com.yxboot.ai.document.splitter.ParentChildSplitter;
import com.yxboot.ai.document.splitter.SplitMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档加载与分段服务。PDF 使用 PDFBox 专用加载器，其他格式使用 Tika。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiDocumentProcessingService {

    private final ChapterSplitter chapterSplitter;
    private final CharacterSplitter characterSplitter;
    private final ParentChildSplitter parentChildSplitter;
    private final PdfDocumentLoader pdfDocumentLoader;

    public List<DocumentSegment> loadAndSplitDocument(File file, SplitMode splitMode, Integer maxSegmentLength,
            Integer overlapLength) {
        return loadAndSplitDocument(file, splitMode, maxSegmentLength, overlapLength, null);
    }

    public List<DocumentSegment> loadAndSplitDocument(File file, SplitMode splitMode, Integer maxSegmentLength,
            Integer overlapLength, Integer parentChunkSize) {
        List<Document> rawDocs = loadRawDocuments(file);
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

    /**
     * 按文件类型加载文档。PDF 不走 Tika（部分 PDF 会在 Tika 解析阶段阻塞），改用 PDFBox。
     */
    private List<Document> loadRawDocuments(File file) {
        if (isPdfFile(file.getName())) {
            log.info("使用 PDFBox 加载 PDF 文档: {}", file.getAbsolutePath());
            com.yxboot.ai.document.Document legacyDoc = pdfDocumentLoader.load(file);
            String content = legacyDoc.getContent();
            if (content == null || content.isBlank()) {
                return List.of();
            }
            return List.of(new Document(content, legacyDoc.getMetadata()));
        }
        return new TikaDocumentReader(new FileSystemResource(file)).get();
    }

    private static boolean isPdfFile(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }
}
