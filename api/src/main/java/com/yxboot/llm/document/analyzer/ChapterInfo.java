package com.yxboot.llm.document.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 章节信息数据结构 包含章节的标题、内容、层级关系等信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterInfo {

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节内容
     */
    private String content;

    /**
     * 章节级别（1为一级标题，2为二级标题等）
     */
    private int level;

    /**
     * 在文档中的起始位置
     */
    private int startPosition;

    /**
     * 在文档中的结束位置
     */
    private int endPosition;

    /**
     * 页码（仅PDF有效）
     */
    private Integer pageNumber;

    /**
     * 子章节
     */
    @Builder.Default
    private List<ChapterInfo> subChapters = new ArrayList<>();

    /**
     * 额外元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 添加子章节
     * 
     * @param subChapter 子章节
     */
    public void addSubChapter(ChapterInfo subChapter) {
        if (this.subChapters == null) {
            this.subChapters = new ArrayList<>();
        }
        this.subChapters.add(subChapter);
    }

    /**
     * 是否有子章节
     * 
     * @return 是否有子章节
     */
    public boolean hasSubChapters() {
        return subChapters != null && !subChapters.isEmpty();
    }

    /**
     * 获取章节的完整内容（包含子章节）
     * 
     * @return 完整内容
     */
    public String getFullContent() {
        StringBuilder fullContent = new StringBuilder();

        if (content != null && !content.trim().isEmpty()) {
            fullContent.append(content);
        }

        if (hasSubChapters()) {
            for (ChapterInfo subChapter : subChapters) {
                String subContent = subChapter.getFullContent();
                if (!subContent.trim().isEmpty()) {
                    if (fullContent.length() > 0) {
                        fullContent.append("\n\n");
                    }
                    fullContent.append(subContent);
                }
            }
        }

        return fullContent.toString();
    }

    /**
     * 获取扁平化的所有章节（包括子章节）
     * 
     * @return 所有章节的平面列表
     */
    public List<ChapterInfo> getAllChapters() {
        List<ChapterInfo> allChapters = new ArrayList<>();
        allChapters.add(this);

        if (hasSubChapters()) {
            for (ChapterInfo subChapter : subChapters) {
                allChapters.addAll(subChapter.getAllChapters());
            }
        }

        return allChapters;
    }
}
