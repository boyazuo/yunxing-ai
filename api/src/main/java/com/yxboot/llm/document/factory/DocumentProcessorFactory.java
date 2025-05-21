package com.yxboot.llm.document.factory;

import org.springframework.stereotype.Component;

import com.yxboot.llm.document.loader.DocumentLoader;
import com.yxboot.llm.document.loader.PdfDocumentLoader;
import com.yxboot.llm.document.loader.WordDocumentLoader;
import com.yxboot.llm.document.splitter.CharacterLengthSplitter;
import com.yxboot.llm.document.splitter.DocumentSplitter;

import lombok.RequiredArgsConstructor;

/**
 * 文档处理工厂类
 * 用于创建不同类型的文档加载器和分割器
 */
@Component
@RequiredArgsConstructor
public class DocumentProcessorFactory {

    private final PdfDocumentLoader pdfDocumentLoader;
    private final WordDocumentLoader wordDocumentLoader;

    /**
     * 获取PDF文档加载器
     * 
     * @return PDF文档加载器
     */
    public DocumentLoader getPdfLoader() {
        return pdfDocumentLoader;
    }

    /**
     * 获取Word文档加载器
     * 
     * @return Word文档加载器
     */
    public DocumentLoader getWordLoader() {
        return wordDocumentLoader;
    }

    /**
     * 根据文件扩展名获取适合的加载器
     * 
     * @param extension 文件扩展名
     * @return 文档加载器
     */
    public DocumentLoader getLoaderByExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            throw new IllegalArgumentException("文件扩展名不能为空");
        }

        extension = extension.toLowerCase();

        if ("pdf".equals(extension)) {
            return pdfDocumentLoader;
        } else if ("docx".equals(extension)) {
            return wordDocumentLoader;
        }

        throw new IllegalArgumentException("不支持的文件扩展名：" + extension);
    }

    /**
     * 创建默认配置的字符长度分割器
     * 
     * @return 字符长度分割器
     */
    public DocumentSplitter createDefaultSplitter() {
        return new CharacterLengthSplitter();
    }

    /**
     * 创建自定义配置的字符长度分割器
     * 
     * @param maxChunkSize 最大块大小
     * @param overlapSize  块重叠大小
     * @return 字符长度分割器
     */
    public DocumentSplitter createSplitter(int maxChunkSize, int overlapSize) {
        return new CharacterLengthSplitter(maxChunkSize, overlapSize);
    }
}