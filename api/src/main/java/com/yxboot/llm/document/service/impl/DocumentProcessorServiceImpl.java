package com.yxboot.llm.document.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import com.yxboot.llm.document.Document;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.llm.document.loader.DocumentLoader;
import com.yxboot.llm.document.loader.PdfDocumentLoader;
import com.yxboot.llm.document.loader.WordDocumentLoader;
import com.yxboot.llm.document.service.DocumentProcessorService;
import com.yxboot.llm.document.splitter.CharacterLengthSplitter;
import com.yxboot.llm.document.splitter.DocumentSplitter;

import lombok.RequiredArgsConstructor;

/**
 * 文档处理服务实现
 */
@Service
@RequiredArgsConstructor
public class DocumentProcessorServiceImpl implements DocumentProcessorService {

    private final PdfDocumentLoader pdfDocumentLoader;
    private final WordDocumentLoader wordDocumentLoader;
    private final CharacterLengthSplitter characterLengthSplitter;

    // 扩展名到加载器的映射
    private final Map<String, DocumentLoader> extensionToLoader = new ConcurrentHashMap<>();
    // MIME类型到加载器的映射
    private final Map<String, DocumentLoader> mimeTypeToLoader = new ConcurrentHashMap<>();

    // 用于检测MIME类型
    private final Tika tika = new Tika();

    /**
     * 初始化加载器映射
     */
    public void init() {
        // 注册PDF加载器
        registerLoader(pdfDocumentLoader, "pdf", "application/pdf");
        // 注册Word加载器
        registerLoader(wordDocumentLoader, "docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        // 可以在这里注册更多类型的加载器
    }

    /**
     * 注册加载器
     * 
     * @param loader    加载器
     * @param extension 文件扩展名
     * @param mimeType  MIME类型
     */
    private void registerLoader(DocumentLoader loader, String extension, String mimeType) {
        extensionToLoader.put(extension.toLowerCase(), loader);
        mimeTypeToLoader.put(mimeType.toLowerCase(), loader);
    }

    /**
     * 根据文件获取适合的加载器
     * 
     * @param file 文件
     * @return 加载器
     */
    private DocumentLoader getLoaderForFile(File file) {
        String filename = file.getName();
        String extension = getFileExtension(filename);

        // 首先尝试通过扩展名获取加载器
        DocumentLoader loader = extensionToLoader.get(extension.toLowerCase());
        if (loader != null) {
            return loader;
        }

        // 如果扩展名不匹配，尝试通过MIME类型识别
        try {
            String mimeType = tika.detect(file).toLowerCase();
            return mimeTypeToLoader.get(mimeType);
        } catch (IOException e) {
            throw new RuntimeException("无法识别文件类型：" + filename, e);
        }
    }

    /**
     * 根据文件名和内容获取适合的加载器
     * 
     * @param bytes    文件内容
     * @param filename 文件名
     * @return 加载器
     */
    private DocumentLoader getLoaderForContent(byte[] bytes, String filename) {
        String extension = getFileExtension(filename);

        // 首先尝试通过扩展名获取加载器
        DocumentLoader loader = extensionToLoader.get(extension.toLowerCase());
        if (loader != null) {
            return loader;
        }

        // 如果扩展名不匹配，尝试通过MIME类型识别
        try {
            String mimeType = tika.detect(bytes).toLowerCase();
            return mimeTypeToLoader.get(mimeType);
        } catch (Exception e) {
            throw new RuntimeException("无法识别文件类型：" + filename, e);
        }
    }

    /**
     * 获取文件扩展名
     * 
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    @Override
    public Document loadDocument(File file) {
        // 延迟初始化
        if (extensionToLoader.isEmpty()) {
            init();
        }

        DocumentLoader loader = getLoaderForFile(file);
        if (loader == null) {
            throw new RuntimeException("不支持的文件类型：" + file.getName());
        }

        return loader.load(file);
    }

    @Override
    public Document loadDocument(Path path) {
        return loadDocument(path.toFile());
    }

    @Override
    public Document loadDocument(InputStream inputStream, String filename) {
        // 延迟初始化
        if (extensionToLoader.isEmpty()) {
            init();
        }

        // 对于输入流，我们只能通过文件名来识别类型
        String extension = getFileExtension(filename);
        DocumentLoader loader = extensionToLoader.get(extension.toLowerCase());

        if (loader == null) {
            throw new RuntimeException("不支持的文件类型：" + filename);
        }

        return loader.load(inputStream, filename);
    }

    @Override
    public Document loadDocument(byte[] bytes, String filename) {
        // 延迟初始化
        if (extensionToLoader.isEmpty()) {
            init();
        }

        DocumentLoader loader = getLoaderForContent(bytes, filename);
        if (loader == null) {
            throw new RuntimeException("不支持的文件类型：" + filename);
        }

        return loader.load(bytes, filename);
    }

    @Override
    public List<DocumentSegment> splitDocument(Document document) {
        return characterLengthSplitter.split(document);
    }

    @Override
    public List<DocumentSegment> splitDocument(Document document, int maxChunkSize, int overlapSize) {
        DocumentSplitter splitter = new CharacterLengthSplitter(maxChunkSize, overlapSize);
        return splitter.split(document);
    }

    @Override
    public List<DocumentSegment> loadAndSplitDocument(File file) {
        Document document = loadDocument(file);
        return splitDocument(document);
    }

    @Override
    public List<DocumentSegment> loadAndSplitDocument(Path path) {
        Document document = loadDocument(path);
        return splitDocument(document);
    }

    @Override
    public List<DocumentSegment> loadAndSplitDocument(InputStream inputStream, String filename) {
        Document document = loadDocument(inputStream, filename);
        return splitDocument(document);
    }

    @Override
    public List<DocumentSegment> loadAndSplitDocument(File file, int maxChunkSize, int overlapSize) {
        Document document = loadDocument(file);
        return splitDocument(document, maxChunkSize, overlapSize);
    }
}