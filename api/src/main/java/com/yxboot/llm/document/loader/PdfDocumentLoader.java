package com.yxboot.llm.document.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import com.yxboot.llm.document.Document;

/**
 * PDF文档加载器
 */
@Component
public class PdfDocumentLoader extends AbstractDocumentLoader {

    @Override
    protected Document loadWithMetadata(InputStream inputStream, Map<String, Object> metadata) {
        try {
            // 需要先读取为字节数组，PDFBox 3.0.2版本不再直接支持InputStream
            byte[] pdfBytes = inputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // 添加元数据
                metadata.put("page_count", document.getNumberOfPages());
                metadata.put("document_type", "pdf");

                return Document.of(text, metadata);
            }
        } catch (IOException e) {
            throw new RuntimeException("PDF文档解析失败", e);
        }
    }

    /**
     * 检查文件扩展名是否为PDF
     * 
     * @param extension 文件扩展名
     * @return 是否为PDF
     */
    public boolean supportedExtension(String extension) {
        return "pdf".equalsIgnoreCase(extension);
    }
}