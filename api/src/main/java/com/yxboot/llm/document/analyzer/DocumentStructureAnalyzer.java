package com.yxboot.llm.document.analyzer;

import java.util.List;

/**
 * 文档结构分析器接口 用于分析不同类型文档的结构信息，提取章节层次
 */
public interface DocumentStructureAnalyzer {

    /**
     * 分析文档结构，提取章节信息
     * 
     * @param document 文档对象（可能是字节数组、文档对象等）
     * @return 章节信息列表
     */
    List<ChapterInfo> analyzeStructure(Object document);

    /**
     * 检查是否支持该类型的文档
     * 
     * @param documentType 文档类型
     * @return 是否支持
     */
    boolean supports(String documentType);
}
