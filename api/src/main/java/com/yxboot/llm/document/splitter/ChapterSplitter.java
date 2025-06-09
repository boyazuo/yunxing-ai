package com.yxboot.llm.document.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.yxboot.llm.document.Document;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.llm.document.analyzer.ChapterInfo;
import com.yxboot.llm.document.analyzer.PdfStructureAnalyzer;
import com.yxboot.llm.document.analyzer.TextStructureAnalyzer;
import com.yxboot.llm.document.analyzer.WordStructureAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 智能章节分割器 基于文档结构信息进行智能章节分割
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChapterSplitter extends AbstractSplitter {

    private final PdfStructureAnalyzer pdfAnalyzer;
    private final WordStructureAnalyzer wordAnalyzer;
    private final TextStructureAnalyzer textAnalyzer;

    /**
     * 是否包含子章节
     */
    private boolean includeSubChapters = true;

    /**
     * 最小章节长度
     */
    private int minChapterLength = 100;

    /**
     * 最大章节长度（超过此长度会进一步分割）
     */
    private int maxChapterLength = 5000;

    @Override
    public List<DocumentSegment> split(Document document) {
        if (document == null || document.getContent() == null || document.getContent().trim().isEmpty()) {
            return new ArrayList<>();
        }

        log.info("开始智能章节分割，文档类型: {}", document.getMetadata().get("document_type"));

        String documentType = (String) document.getMetadata().get("document_type");
        List<ChapterInfo> chapters;

        // 根据文档类型选择合适的分析器
        if ("pdf".equals(documentType)) {
            chapters = analyzePdfStructure(document);
        } else if ("docx".equals(documentType)) {
            chapters = analyzeWordStructure(document);
        } else {
            chapters = analyzeTextStructure(document.getContent());
        }

        log.info("章节分析完成，发现 {} 个章节", chapters.size());

        // 转换为文档分段
        List<DocumentSegment> segments = convertToSegments(chapters);

        // 添加文档元数据到每个分段
        enhanceSegmentsMetadata(segments, document.getMetadata());

        log.info("智能章节分割完成，生成 {} 个分段", segments.size());
        return segments;
    }

    @Override
    protected List<String> splitText(String text) {
        List<ChapterInfo> chapters = textAnalyzer.analyzeTextStructure(text);
        return chapters.stream()
                .map(chapter -> includeSubChapters ? chapter.getFullContent() : chapter.getContent())
                .filter(content -> content != null && content.trim().length() >= minChapterLength)
                .toList();
    }

    /**
     * 分析PDF文档结构
     */
    private List<ChapterInfo> analyzePdfStructure(Document document) {
        try {
            // 从元数据获取原始字节数据
            byte[] pdfBytes = (byte[]) document.getMetadata().get("raw_bytes");
            if (pdfBytes != null) {
                return pdfAnalyzer.analyzeStructure(pdfBytes);
            } else {
                log.warn("PDF文档缺少原始字节数据，回退到文本分析");
            }
        } catch (Exception e) {
            log.warn("PDF结构分析失败，回退到文本分析", e);
        }

        return textAnalyzer.analyzeTextStructure(document.getContent());
    }

    /**
     * 分析Word文档结构
     */
    private List<ChapterInfo> analyzeWordStructure(Document document) {
        try {
            // 从元数据获取原始字节数据
            byte[] docBytes = (byte[]) document.getMetadata().get("raw_bytes");
            if (docBytes != null) {
                return wordAnalyzer.analyzeStructure(docBytes);
            } else {
                log.warn("Word文档缺少原始字节数据，回退到文本分析");
            }
        } catch (Exception e) {
            log.warn("Word结构分析失败，回退到文本分析", e);
        }

        return textAnalyzer.analyzeTextStructure(document.getContent());
    }

    /**
     * 分析文本结构
     */
    private List<ChapterInfo> analyzeTextStructure(String content) {
        return textAnalyzer.analyzeTextStructure(content);
    }

    /**
     * 将章节信息转换为文档分段
     */
    private List<DocumentSegment> convertToSegments(List<ChapterInfo> chapters) {
        List<DocumentSegment> segments = new ArrayList<>();
        int segmentIndex = 0;

        for (ChapterInfo chapter : chapters) {
            if (includeSubChapters) {
                // 包含子章节的完整内容
                String fullContent = chapter.getFullContent();
                if (fullContent.length() >= minChapterLength) {
                    if (fullContent.length() <= maxChapterLength) {
                        segments.add(createSegment(chapter, fullContent, segmentIndex++));
                    } else {
                        // 章节太长，需要进一步分割
                        List<DocumentSegment> splitSegments = splitLargeChapter(chapter, segmentIndex);
                        segments.addAll(splitSegments);
                        segmentIndex += splitSegments.size();
                    }
                }
            } else {
                // 只包含当前级别的内容
                if (chapter.getContent() != null && chapter.getContent().length() >= minChapterLength) {
                    segments.add(createSegment(chapter, chapter.getContent(), segmentIndex++));
                }

                // 递归处理子章节
                if (chapter.hasSubChapters()) {
                    List<DocumentSegment> subSegments = convertToSegments(chapter.getSubChapters());
                    // 更新子分段的索引
                    for (DocumentSegment subSegment : subSegments) {
                        subSegment.getMetadata().put("segment_index", segmentIndex++);
                    }
                    segments.addAll(subSegments);
                }
            }
        }

        return segments;
    }

    /**
     * 分割过大的章节
     */
    private List<DocumentSegment> splitLargeChapter(ChapterInfo chapter, int startIndex) {
        List<DocumentSegment> segments = new ArrayList<>();
        String content = chapter.getFullContent();

        log.debug("章节 '{}' 长度 {} 超过最大限制 {}，进行进一步分割",
                chapter.getTitle(), content.length(), maxChapterLength);

        // 使用字符长度分割器进一步分割
        CharacterSplitter lengthSplitter = new CharacterSplitter(maxChapterLength, 200);
        List<String> chunks = lengthSplitter.splitText(content);

        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            DocumentSegment segment = DocumentSegment.builder()
                    .id(generateSegmentId())
                    .title(generateChunkTitle(chapter.getTitle(), i + 1, chunks.size()))
                    .content(chunkContent)
                    .build();

            // 添加章节元数据
            segment.addMetadata("chapter_title", chapter.getTitle());
            segment.addMetadata("chapter_level", chapter.getLevel());
            segment.addMetadata("chunk_index", i + 1);
            segment.addMetadata("total_chunks", chunks.size());
            segment.addMetadata("splitting_reason", "large_chapter");
            segment.addMetadata("original_chapter_length", content.length());

            // 复制原章节的元数据
            chapter.getMetadata().forEach(segment::addMetadata);

            segments.add(segment);
        }

        log.debug("大章节分割完成，分成 {} 个子分段", segments.size());
        return segments;
    }

    /**
     * 生成分块标题
     */
    private String generateChunkTitle(String originalTitle, int chunkIndex, int totalChunks) {
        if (totalChunks <= 1) {
            return originalTitle;
        }
        return String.format("%s (第%d部分)", originalTitle, chunkIndex);
    }

    /**
     * 创建文档分段
     */
    private DocumentSegment createSegment(ChapterInfo chapter, String content, int index) {
        DocumentSegment segment = DocumentSegment.builder()
                .id(generateSegmentId())
                .title(chapter.getTitle())
                .content(content)
                .build();

        // 添加章节元数据
        segment.addMetadata("chapter_level", chapter.getLevel());
        segment.addMetadata("chapter_index", index);
        segment.addMetadata("start_position", chapter.getStartPosition());
        segment.addMetadata("end_position", chapter.getEndPosition());
        segment.addMetadata("splitting_method", "smart_chapter");

        if (chapter.getPageNumber() != null) {
            segment.addMetadata("page_number", chapter.getPageNumber());
        }

        // 复制章节的额外元数据
        chapter.getMetadata().forEach(segment::addMetadata);

        return segment;
    }

    /**
     * 增强分段元数据
     */
    private void enhanceSegmentsMetadata(List<DocumentSegment> segments, Map<String, Object> documentMetadata) {
        for (int i = 0; i < segments.size(); i++) {
            DocumentSegment segment = segments.get(i);

            // 添加分段索引信息
            if (!segment.getMetadata().containsKey("segment_index")) {
                segment.addMetadata("segment_index", i);
            }
            segment.addMetadata("total_segments", segments.size());

            // 复制文档元数据
            documentMetadata.forEach((key, value) -> {
                if (!segment.getMetadata().containsKey(key)) {
                    segment.addMetadata(key, value);
                }
            });
        }
    }

    @Override
    protected String generateSegmentTitle(int index) {
        return "智能章节 " + (index + 1);
    }

    // 配置方法
    public ChapterSplitter setIncludeSubChapters(boolean includeSubChapters) {
        this.includeSubChapters = includeSubChapters;
        return this;
    }

    public ChapterSplitter setMinChapterLength(int minChapterLength) {
        this.minChapterLength = Math.max(minChapterLength, 10);
        return this;
    }

    public ChapterSplitter setMaxChapterLength(int maxChapterLength) {
        this.maxChapterLength = Math.max(maxChapterLength, minChapterLength + 100);
        return this;
    }

    // Getter方法，用于依赖注入
    public PdfStructureAnalyzer getPdfAnalyzer() {
        return pdfAnalyzer;
    }

    public WordStructureAnalyzer getWordAnalyzer() {
        return wordAnalyzer;
    }

    public TextStructureAnalyzer getTextAnalyzer() {
        return textAnalyzer;
    }
}
