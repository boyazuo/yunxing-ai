package com.yxboot.llm.document.analyzer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Word文档结构分析器 利用Word文档的样式信息提取章节结构
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WordStructureAnalyzer implements DocumentStructureAnalyzer {

    private final TextStructureAnalyzer textAnalyzer;

    @Override
    public List<ChapterInfo> analyzeStructure(Object document) {
        if (!(document instanceof byte[])) {
            throw new IllegalArgumentException("Word分析器只支持字节数组格式的文档");
        }

        byte[] docBytes = (byte[]) document;
        List<ChapterInfo> chapters = new ArrayList<>();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(docBytes);
                XWPFDocument doc = new XWPFDocument(bis)) {

            log.info("开始分析Word文档结构，总段落数: {}", doc.getParagraphs().size());

            chapters = extractFromStyles(doc);

            // 如果没有找到样式标题，使用文本分析
            if (chapters.isEmpty()) {
                log.info("Word文档没有标准样式标题，使用智能文本分析");
                chapters = extractFromTextAnalysis(doc);
            }

            log.info("Word结构分析完成，提取到 {} 个章节", chapters.size());

        } catch (IOException e) {
            log.error("Word文档结构分析失败", e);
            throw new RuntimeException("Word文档结构分析失败", e);
        }

        return chapters;
    }

    @Override
    public boolean supports(String documentType) {
        return "docx".equalsIgnoreCase(documentType);
    }

    /**
     * 从Word样式提取章节信息
     */
    private List<ChapterInfo> extractFromStyles(XWPFDocument document) {
        List<ChapterInfo> chapters = new ArrayList<>();
        ChapterInfo currentChapter = null;
        StringBuilder contentBuilder = new StringBuilder();

        List<XWPFParagraph> paragraphs = document.getParagraphs();

        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph paragraph = paragraphs.get(i);
            String text = paragraph.getText().trim();

            if (text.isEmpty()) {
                contentBuilder.append("\n");
                continue;
            }

            // 检查是否为标题样式
            String styleName = paragraph.getStyle();
            int headingLevel = getHeadingLevel(paragraph, styleName);

            if (headingLevel > 0) {
                log.debug("发现标题: {} (级别: {}, 样式: {})", text, headingLevel, styleName);

                // 保存之前的章节
                if (currentChapter != null) {
                    String content = contentBuilder.toString().trim();
                    if (!content.isEmpty()) {
                        currentChapter.setContent(content);
                        currentChapter.setEndPosition(i - 1);
                        chapters.add(currentChapter);
                    }
                }

                // 创建新章节
                currentChapter = ChapterInfo.builder()
                        .title(text)
                        .level(headingLevel)
                        .startPosition(i)
                        .build();

                currentChapter.getMetadata().put("source", "word_style");
                currentChapter.getMetadata().put("style_name", styleName);

                contentBuilder = new StringBuilder();
            } else {
                // 添加到当前章节内容
                if (contentBuilder.length() > 0) {
                    contentBuilder.append("\n");
                }
                contentBuilder.append(text);
            }
        }

        // 保存最后一个章节
        if (currentChapter != null) {
            String content = contentBuilder.toString().trim();
            if (!content.isEmpty()) {
                currentChapter.setContent(content);
                currentChapter.setEndPosition(paragraphs.size() - 1);
                chapters.add(currentChapter);
            }
        }

        log.info("从Word样式中提取到 {} 个章节", chapters.size());
        return buildHierarchy(chapters);
    }

    /**
     * 获取标题级别
     */
    private int getHeadingLevel(XWPFParagraph paragraph, String styleName) {
        if (styleName == null) {
            return 0;
        }

        styleName = styleName.toLowerCase();

        // 检查标准的标题样式
        if (styleName.startsWith("heading")) {
            try {
                String levelStr = styleName.replaceAll("\\D", "");
                if (!levelStr.isEmpty()) {
                    int level = Integer.parseInt(levelStr);
                    return Math.min(level, 6); // 限制最大级别为6
                }
            } catch (NumberFormatException e) {
                // 忽略
            }
        }

        // 检查中文标题样式
        if (styleName.contains("标题")) {
            if (styleName.contains("1"))
                return 1;
            if (styleName.contains("2"))
                return 2;
            if (styleName.contains("3"))
                return 3;
            if (styleName.contains("4"))
                return 4;
            if (styleName.contains("5"))
                return 5;
            if (styleName.contains("6"))
                return 6;
            return 1; // 默认为一级标题
        }

        // 检查其他可能的标题样式名称
        if (styleName.contains("title") || styleName.contains("header")) {
            return 1;
        }

        return 0;
    }

    /**
     * 构建章节层次结构
     */
    private List<ChapterInfo> buildHierarchy(List<ChapterInfo> flatChapters) {
        if (flatChapters.isEmpty()) {
            return flatChapters;
        }

        List<ChapterInfo> result = new ArrayList<>();
        List<ChapterInfo> stack = new ArrayList<>();

        for (ChapterInfo chapter : flatChapters) {
            // 清理栈中级别大于等于当前章节级别的项
            while (!stack.isEmpty() && stack.get(stack.size() - 1).getLevel() >= chapter.getLevel()) {
                stack.remove(stack.size() - 1);
            }

            if (stack.isEmpty()) {
                // 顶级章节
                result.add(chapter);
            } else {
                // 子章节
                stack.get(stack.size() - 1).addSubChapter(chapter);
            }

            stack.add(chapter);
        }

        log.info("构建层次结构完成，顶级章节数: {}", result.size());
        return result;
    }

    /**
     * 基于智能文本分析提取章节信息
     */
    private List<ChapterInfo> extractFromTextAnalysis(XWPFDocument document) {
        StringBuilder fullText = new StringBuilder();

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String text = paragraph.getText().trim();
            if (!text.isEmpty()) {
                fullText.append(text).append("\n");
            }
        }

        // 使用智能文本分析器
        List<ChapterInfo> chapters = textAnalyzer.analyzeTextStructure(fullText.toString());

        // 为章节添加Word特有的元数据
        for (ChapterInfo chapter : chapters) {
            chapter.getMetadata().put("source", "word_text_analysis");
        }

        return chapters;
    }
}
