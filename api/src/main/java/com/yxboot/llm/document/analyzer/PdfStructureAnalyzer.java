package com.yxboot.llm.document.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PDF文档结构分析器 利用PDF书签和格式信息提取章节结构
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfStructureAnalyzer implements DocumentStructureAnalyzer {

    private final TextStructureAnalyzer textAnalyzer;

    @Override
    public List<ChapterInfo> analyzeStructure(Object document) {
        if (!(document instanceof byte[])) {
            throw new IllegalArgumentException("PDF分析器只支持字节数组格式的文档");
        }

        byte[] pdfBytes = (byte[]) document;
        List<ChapterInfo> chapters = new ArrayList<>();

        try (PDDocument pdDocument = Loader.loadPDF(pdfBytes)) {
            log.info("开始分析PDF文档结构，总页数: {}", pdDocument.getNumberOfPages());

            // 首先尝试从书签提取章节信息
            chapters = extractFromBookmarks(pdDocument);

            // 如果没有书签，则回退到智能文本分析
            if (chapters.isEmpty()) {
                log.info("PDF文档没有书签信息，使用智能文本分析");
                chapters = extractFromTextAnalysis(pdDocument);
            }

            log.info("PDF结构分析完成，提取到 {} 个章节", chapters.size());

        } catch (IOException e) {
            log.error("PDF文档结构分析失败", e);
            throw new RuntimeException("PDF文档结构分析失败", e);
        }

        return chapters;
    }

    @Override
    public boolean supports(String documentType) {
        return "pdf".equalsIgnoreCase(documentType);
    }

    /**
     * 从PDF书签提取章节信息
     */
    private List<ChapterInfo> extractFromBookmarks(PDDocument document) throws IOException {
        List<ChapterInfo> chapters = new ArrayList<>();
        PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();

        if (outline == null) {
            log.info("PDF文档没有书签大纲");
            return chapters;
        }

        log.info("PDF文档包含书签信息，开始提取章节结构");

        // 获取全文用于内容提取
        PDFTextStripper textStripper = new PDFTextStripper();
        String fullText = textStripper.getText(document);

        // 遍历顶级书签项
        PDOutlineItem item = outline.getFirstChild();
        while (item != null) {
            ChapterInfo chapter = extractChapterFromBookmark(item, document, fullText, 1);
            if (chapter != null) {
                chapters.add(chapter);
            }
            item = item.getNextSibling();
        }

        return chapters;
    }

    /**
     * 从书签项提取章节信息
     */
    private ChapterInfo extractChapterFromBookmark(PDOutlineItem item, PDDocument document,
            String fullText, int level) throws IOException {

        String title = item.getTitle();
        if (title == null || title.trim().isEmpty()) {
            return null;
        }

        title = title.trim();
        log.debug("处理书签: {} (级别: {})", title, level);

        ChapterInfo.ChapterInfoBuilder builder = ChapterInfo.builder()
                .title(title)
                .level(level);

        // 提取章节内容
        String content = extractContentForBookmark(item, fullText);
        builder.content(content);

        // 设置位置信息
        int titleIndex = fullText.indexOf(title);
        if (titleIndex >= 0) {
            builder.startPosition(titleIndex);
            builder.endPosition(titleIndex + content.length());
        }

        ChapterInfo chapter = builder.build();
        chapter.getMetadata().put("source", "pdf_bookmark");

        // 处理子书签
        PDOutlineItem child = item.getFirstChild();
        while (child != null) {
            ChapterInfo subChapter = extractChapterFromBookmark(child, document, fullText, level + 1);
            if (subChapter != null) {
                chapter.addSubChapter(subChapter);
            }
            child = child.getNextSibling();
        }

        return chapter;
    }

    /**
     * 为书签提取对应的内容
     */
    private String extractContentForBookmark(PDOutlineItem item, String fullText) {
        String title = item.getTitle().trim();

        // 在全文中查找标题位置
        int titleIndex = fullText.indexOf(title);
        if (titleIndex == -1) {
            // 尝试模糊匹配
            titleIndex = findApproximateTitle(fullText, title);
        }

        if (titleIndex == -1) {
            log.warn("无法在文档中找到书签标题: {}", title);
            return "";
        }

        // 查找下一个同级或上级标题的位置作为结束点
        int endIndex = findNextChapterStart(fullText, titleIndex + title.length(), item);
        if (endIndex == -1) {
            endIndex = fullText.length();
        }

        String content = fullText.substring(titleIndex + title.length(), endIndex).trim();

        // 移除标题行
        if (content.startsWith("\n")) {
            content = content.substring(1);
        }

        return content;
    }

    /**
     * 模糊查找标题位置
     */
    private int findApproximateTitle(String fullText, String title) {
        // 移除标点符号和多余空格后查找
        String cleanTitle = title.replaceAll("[\\p{Punct}\\s]+", " ").trim();
        String cleanText = fullText.replaceAll("[\\p{Punct}\\s]+", " ");

        int index = cleanText.indexOf(cleanTitle);
        if (index >= 0) {
            // 回到原始文本中找到对应位置
            int originalIndex = 0;
            int cleanIndex = 0;
            for (int i = 0; i < fullText.length() && cleanIndex < index; i++) {
                char c = fullText.charAt(i);
                if (!Character.isWhitespace(c) && Character.isLetterOrDigit(c)) {
                    cleanIndex++;
                }
                originalIndex = i;
            }
            return originalIndex;
        }

        return -1;
    }

    /**
     * 查找下一章节开始位置
     */
    private int findNextChapterStart(String fullText, int startIndex, PDOutlineItem currentItem) {
        // 查找兄弟节点
        PDOutlineItem nextSibling = currentItem.getNextSibling();
        if (nextSibling != null && nextSibling.getTitle() != null) {
            int nextIndex = fullText.indexOf(nextSibling.getTitle(), startIndex);
            if (nextIndex != -1) {
                return nextIndex;
            }
        }

        // 简化处理，不处理父节点的兄弟节点，避免类型转换问题

        return -1;
    }

    /**
     * 基于智能文本分析提取章节信息
     */
    private List<ChapterInfo> extractFromTextAnalysis(PDDocument document) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        String fullText = textStripper.getText(document);

        // 使用智能文本分析器
        List<ChapterInfo> chapters = textAnalyzer.analyzeTextStructure(fullText);

        // 为章节添加PDF特有的元数据
        for (ChapterInfo chapter : chapters) {
            chapter.getMetadata().put("source", "pdf_text_analysis");
            // 可以在这里添加更多PDF特有的信息，比如页码等
        }

        return chapters;
    }
}
