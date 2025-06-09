package com.yxboot.llm.document.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * 智能文本结构分析器 基于文本统计特征和模式识别进行章节分析
 */
@Slf4j
@Component
public class TextStructureAnalyzer implements DocumentStructureAnalyzer {

    // 可能的标题模式（优先级从高到低）
    private static final List<TitlePattern> TITLE_PATTERNS = List.of(
            // 明确的章节标识
            new TitlePattern(Pattern.compile("^\\s*第[一二三四五六七八九十百千万\\d]+[章节部分]\\s*[：:：]?\\s*(.*)$", Pattern.MULTILINE), 1, 0.95),
            new TitlePattern(Pattern.compile("^\\s*Chapter\\s+\\d+[\\s\\.:：]?\\s*(.*)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE), 1, 0.9),

            // 数字编号
            new TitlePattern(Pattern.compile("^\\s*(\\d+)[\\s\\.、]\\s*(.+)$", Pattern.MULTILINE), 1, 0.8),
            new TitlePattern(Pattern.compile("^\\s*(\\d+\\.\\d+)[\\s]\\s*(.+)$", Pattern.MULTILINE), 2, 0.8),
            new TitlePattern(Pattern.compile("^\\s*(\\d+\\.\\d+\\.\\d+)[\\s]\\s*(.+)$", Pattern.MULTILINE), 3, 0.8),

            // 中文序号
            new TitlePattern(Pattern.compile("^\\s*[一二三四五六七八九十百千万]+[、\\.]\\s*(.+)$", Pattern.MULTILINE), 1, 0.7),

            // 括号编号
            new TitlePattern(Pattern.compile("^\\s*[（(]\\s*(\\d+)\\s*[）)]\\s*(.+)$", Pattern.MULTILINE), 1, 0.6),

            // 罗马数字
            new TitlePattern(Pattern.compile("^\\s*([IVXLCDM]+)[\\s\\.、]\\s*(.+)$", Pattern.MULTILINE), 1, 0.5));

    // 标题关键词
    private static final List<String> TITLE_KEYWORDS = List.of(
            "概述", "引言", "前言", "序言", "摘要", "总结", "结论", "附录", "参考文献", "目录");

    @Override
    public List<ChapterInfo> analyzeStructure(Object document) {
        if (!(document instanceof String)) {
            throw new IllegalArgumentException("文本分析器只支持字符串类型的文档");
        }

        String text = (String) document;
        return analyzeTextStructure(text);
    }

    @Override
    public boolean supports(String documentType) {
        return "text".equalsIgnoreCase(documentType) || documentType == null;
    }

    /**
     * 分析文本结构
     */
    public List<ChapterInfo> analyzeTextStructure(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        log.info("开始分析文本结构，文本长度: {}", text.length());

        // 1. 基于模式匹配查找潜在标题
        List<TitleCandidate> candidates = findTitleCandidates(text);
        log.info("找到 {} 个标题候选项", candidates.size());

        // 2. 基于统计特征过滤和评分
        candidates = filterAndScoreCandidates(candidates, text);
        log.info("过滤后剩余 {} 个有效标题候选项", candidates.size());

        // 3. 构建章节结构
        List<ChapterInfo> chapters = buildChapterStructure(candidates, text);
        log.info("构建完成，共 {} 个章节", chapters.size());

        return chapters;
    }

    /**
     * 查找标题候选项
     */
    private List<TitleCandidate> findTitleCandidates(String text) {
        List<TitleCandidate> candidates = new ArrayList<>();

        for (TitlePattern pattern : TITLE_PATTERNS) {
            Matcher matcher = pattern.pattern.matcher(text);
            while (matcher.find()) {
                String fullMatch = matcher.group(0).trim();
                String title = extractTitle(matcher);

                if (isValidTitle(title)) {
                    candidates.add(new TitleCandidate(
                            title,
                            fullMatch,
                            matcher.start(),
                            matcher.end(),
                            pattern.level,
                            pattern.confidence));
                }
            }
        }

        // 按位置排序并去重
        candidates.sort((a, b) -> Integer.compare(a.startPos, b.startPos));
        return deduplicateCandidates(candidates);
    }

    /**
     * 从匹配结果中提取标题
     */
    private String extractTitle(Matcher matcher) {
        // 尝试从捕获组中获取标题
        for (int i = matcher.groupCount(); i >= 1; i--) {
            String group = matcher.group(i);
            if (group != null && !group.trim().isEmpty() && !group.matches("\\d+|[IVXLCDM]+")) {
                return group.trim();
            }
        }

        // 如果没有合适的捕获组，返回整个匹配并清理
        return cleanTitle(matcher.group(0));
    }

    /**
     * 清理标题文本
     */
    private String cleanTitle(String title) {
        return title.replaceAll(
                "^\\s*(?:第[一二三四五六七八九十百千万\\d]+[章节部分]|\\d+[\\s\\.、]|Chapter\\s+\\d+|[IVXLCDM]+[\\s\\.、]|[一二三四五六七八九十百千万]+[、.]|[（(]\\s*\\d+\\s*[）)])\\s*[：:：]?\\s*",
                "")
                .trim();
    }

    /**
     * 验证标题是否有效
     */
    private boolean isValidTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        title = title.trim();

        // 长度检查
        if (title.length() < 2 || title.length() > 200) {
            return false;
        }

        // 不应该是纯数字或纯符号
        if (title.matches("^[\\d\\s\\p{Punct}]+$")) {
            return false;
        }

        // 包含关键词的提高分数
        if (TITLE_KEYWORDS.stream().anyMatch(title::contains)) {
            return true;
        }

        // 不应该包含太多标点符号
        long punctCount = title.chars().filter(c -> Character.getType(c) == Character.OTHER_PUNCTUATION).count();
        if (punctCount > title.length() / 2) {
            return false;
        }

        return true;
    }

    /**
     * 去重候选项
     */
    private List<TitleCandidate> deduplicateCandidates(List<TitleCandidate> candidates) {
        if (candidates.size() <= 1) {
            return candidates;
        }

        List<TitleCandidate> result = new ArrayList<>();
        TitleCandidate prev = null;

        for (TitleCandidate current : candidates) {
            if (prev == null || current.startPos >= prev.startPos + prev.fullMatch.length()) {
                result.add(current);
                prev = current;
            } else if (current.patternConfidence > prev.patternConfidence) {
                // 如果当前候选项置信度更高，替换前一个
                result.set(result.size() - 1, current);
                prev = current;
            }
        }

        return result;
    }

    /**
     * 过滤和评分候选项
     */
    private List<TitleCandidate> filterAndScoreCandidates(List<TitleCandidate> candidates, String text) {
        String[] lines = text.split("\n");

        for (TitleCandidate candidate : candidates) {
            // 计算统计特征分数
            double statisticalScore = calculateStatisticalScore(candidate, text, lines);

            // 更新总分数
            candidate.totalScore = candidate.patternConfidence * 0.6 + statisticalScore * 0.4;
        }

        // 过滤低分候选项
        return candidates.stream()
                .filter(c -> c.totalScore > 0.3)
                .sorted((a, b) -> Double.compare(b.totalScore, a.totalScore))
                .toList();
    }

    /**
     * 计算统计特征分数
     */
    private double calculateStatisticalScore(TitleCandidate candidate, String text, String[] lines) {
        double score = 0.0;

        // 1. 行长度特征（标题通常比较短）
        int lineIndex = findLineIndex(candidate.startPos, text, lines);
        if (lineIndex >= 0 && lineIndex < lines.length) {
            String line = lines[lineIndex].trim();
            if (line.length() < 100) { // 标题通常较短
                score += 0.3;
            }
            if (line.length() < 50) {
                score += 0.2;
            }
        }

        // 2. 位置特征（行首的更可能是标题）
        String beforeText = text.substring(Math.max(0, candidate.startPos - 50), candidate.startPos);
        if (beforeText.contains("\n\n") || beforeText.trim().isEmpty()) {
            score += 0.3;
        }

        // 3. 后续内容特征
        String afterText = text.substring(candidate.endPos, Math.min(text.length(), candidate.endPos + 200));
        if (afterText.startsWith("\n") || afterText.startsWith(" ")) {
            score += 0.2;
        }

        return Math.min(1.0, score);
    }

    /**
     * 查找文本位置对应的行号
     */
    private int findLineIndex(int position, String text, String[] lines) {
        int currentPos = 0;
        for (int i = 0; i < lines.length; i++) {
            int lineEnd = currentPos + lines[i].length() + 1; // +1 for \n
            if (position >= currentPos && position < lineEnd) {
                return i;
            }
            currentPos = lineEnd;
        }
        return -1;
    }

    /**
     * 构建章节结构
     */
    private List<ChapterInfo> buildChapterStructure(List<TitleCandidate> candidates, String text) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        List<ChapterInfo> chapters = new ArrayList<>();

        for (int i = 0; i < candidates.size(); i++) {
            TitleCandidate current = candidates.get(i);
            TitleCandidate next = (i + 1 < candidates.size()) ? candidates.get(i + 1) : null;

            int contentStart = current.endPos;
            int contentEnd = (next != null) ? next.startPos : text.length();

            String content = text.substring(contentStart, contentEnd).trim();

            ChapterInfo chapter = ChapterInfo.builder()
                    .title(current.title)
                    .content(content)
                    .level(current.level)
                    .startPosition(current.startPos)
                    .endPosition(contentEnd)
                    .build();

            chapter.getMetadata().put("confidence", current.totalScore);
            chapter.getMetadata().put("pattern_confidence", current.patternConfidence);

            chapters.add(chapter);
        }

        return buildHierarchy(chapters);
    }

    /**
     * 构建层次结构
     */
    private List<ChapterInfo> buildHierarchy(List<ChapterInfo> flatChapters) {
        if (flatChapters.size() <= 1) {
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
                result.add(chapter);
            } else {
                stack.get(stack.size() - 1).addSubChapter(chapter);
            }

            stack.add(chapter);
        }

        return result;
    }

    /**
     * 标题模式定义
     */
    private static class TitlePattern {
        final Pattern pattern;
        final int level;
        final double confidence;

        TitlePattern(Pattern pattern, int level, double confidence) {
            this.pattern = pattern;
            this.level = level;
            this.confidence = confidence;
        }
    }

    /**
     * 标题候选项
     */
    private static class TitleCandidate {
        final String title;
        final String fullMatch;
        final int startPos;
        final int endPos;
        final int level;
        final double patternConfidence;
        double totalScore;

        TitleCandidate(String title, String fullMatch, int startPos, int endPos, int level, double patternConfidence) {
            this.title = title;
            this.fullMatch = fullMatch;
            this.startPos = startPos;
            this.endPos = endPos;
            this.level = level;
            this.patternConfidence = patternConfidence;
            this.totalScore = patternConfidence;
        }
    }
}
