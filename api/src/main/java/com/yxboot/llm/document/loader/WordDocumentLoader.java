package com.yxboot.llm.document.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import com.yxboot.llm.document.Document;

/**
 * Word文档加载器 支持.docx格式文件
 */
@Component
public class WordDocumentLoader extends AbstractDocumentLoader {

    @Override
    protected Document loadWithMetadata(InputStream inputStream, Map<String, Object> metadata) {
        try {
            // 需要先读取为字节数组，以便保存原始数据用于结构分析
            byte[] docBytes = inputStream.readAllBytes();
            try (XWPFDocument document = new XWPFDocument(new java.io.ByteArrayInputStream(docBytes))) {
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                String text = extractor.getText();
                extractor.close();

                // 添加元数据
                metadata.put("paragraph_count", document.getParagraphs().size());
                metadata.put("document_type", "docx");
                metadata.put("raw_bytes", docBytes); // 保存原始字节数据用于结构分析

                return Document.of(text, metadata);
            }
        } catch (IOException e) {
            throw new RuntimeException("Word文档解析失败", e);
        }
    }

    /**
     * 检查文件扩展名是否为Word
     * 
     * @param extension 文件扩展名
     * @return 是否为Word
     */
    public boolean supportedExtension(String extension) {
        return "docx".equalsIgnoreCase(extension);
    }
}
