package com.yxboot.llm.document.splitter;

import java.util.List;

import com.yxboot.llm.document.Document;
import com.yxboot.llm.document.DocumentSegment;

/**
 * 文档分割器接口
 * 用于将文档分割成多个块
 */
public interface DocumentSplitter {

    /**
     * 将文档分割成多个块
     *
     * @param document 文档对象
     * @return 文档块列表
     */
    List<DocumentSegment> split(Document document);

    /**
     * 将文本分割成多个块
     *
     * @param text 文本内容
     * @return 文档块列表
     */
    List<DocumentSegment> split(String text);
}