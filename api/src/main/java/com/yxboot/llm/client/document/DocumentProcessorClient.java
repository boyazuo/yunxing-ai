package com.yxboot.llm.client.document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import com.yxboot.llm.document.Document;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.llm.document.loader.DocumentLoader;
import com.yxboot.llm.document.loader.PdfDocumentLoader;
import com.yxboot.llm.document.loader.WordDocumentLoader;
import com.yxboot.llm.document.splitter.ChapterSplitter;
import com.yxboot.llm.document.splitter.CharacterSplitter;
import com.yxboot.llm.document.splitter.DocumentSplitter;
import com.yxboot.llm.document.splitter.SplitMode;
import lombok.RequiredArgsConstructor;

/**
 * 文档处理客户端
 */
@Component
@RequiredArgsConstructor
public class DocumentProcessorClient {

    private final PdfDocumentLoader pdfDocumentLoader;
    private final WordDocumentLoader wordDocumentLoader;
    private final CharacterSplitter characterLengthSplitter;
    private final ChapterSplitter smartChapterSplitter;

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
     * @param loader 加载器
     * @param extension 文件扩展名
     * @param mimeType MIME类型
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
     * @param bytes 文件内容
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

    public Document loadDocument(Path path) {
        return loadDocument(path.toFile());
    }

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

    public List<DocumentSegment> splitDocument(Document document) {
        return characterLengthSplitter.split(document);
    }

    public List<DocumentSegment> splitDocument(Document document, int maxChunkSize, int overlapSize) {
        DocumentSplitter splitter = new CharacterSplitter(maxChunkSize, overlapSize);
        return splitter.split(document);
    }

    public List<DocumentSegment> loadAndSplitDocument(File file) {
        Document document = loadDocument(file);
        return splitDocument(document);
    }

    public List<DocumentSegment> loadAndSplitDocument(Path path) {
        Document document = loadDocument(path);
        return splitDocument(document);
    }

    public List<DocumentSegment> loadAndSplitDocument(InputStream inputStream, String filename) {
        Document document = loadDocument(inputStream, filename);
        return splitDocument(document);
    }

    public List<DocumentSegment> loadAndSplitDocument(File file, int maxChunkSize, int overlapSize) {
        Document document = loadDocument(file);
        return splitDocument(document, maxChunkSize, overlapSize);
    }

    /**
     * 使用智能章节分割器分割文档
     * 
     * @param document 文档对象
     * @return 章节分段列表
     */
    public List<DocumentSegment> splitDocumentBySmartChapter(Document document) {
        return smartChapterSplitter.split(document);
    }

    /**
     * 使用自定义配置的智能章节分割器分割文档
     * 
     * @param document 文档对象
     * @param includeSubChapters 是否包含子章节
     * @param minChapterLength 最小章节长度
     * @param maxChapterLength 最大章节长度
     * @return 章节分段列表
     */
    public List<DocumentSegment> splitDocumentBySmartChapter(Document document,
            boolean includeSubChapters, int minChapterLength, int maxChapterLength) {
        ChapterSplitter splitter = new ChapterSplitter(
                smartChapterSplitter.getPdfAnalyzer(),
                smartChapterSplitter.getWordAnalyzer(),
                smartChapterSplitter.getTextAnalyzer())
                        .setIncludeSubChapters(includeSubChapters)
                        .setMinChapterLength(minChapterLength)
                        .setMaxChapterLength(maxChapterLength);

        return splitter.split(document);
    }

    /**
     * 加载并使用智能章节分割器分割文档
     * 
     * @param file 文件对象
     * @return 章节分段列表
     */
    public List<DocumentSegment> loadAndSplitDocumentBySmartChapter(File file) {
        Document document = loadDocument(file);
        return splitDocumentBySmartChapter(document);
    }

    /**
     * 加载并使用自定义配置的智能章节分割器分割文档
     * 
     * @param file 文件对象
     * @param includeSubChapters 是否包含子章节
     * @param minChapterLength 最小章节长度
     * @param maxChapterLength 最大章节长度
     * @return 章节分段列表
     */
    public List<DocumentSegment> loadAndSplitDocumentBySmartChapter(File file,
            boolean includeSubChapters, int minChapterLength, int maxChapterLength) {
        Document document = loadDocument(file);
        return splitDocumentBySmartChapter(document, includeSubChapters, minChapterLength, maxChapterLength);
    }

    /**
     * 根据分段方式加载并分割文档
     * 
     * @param file 文件对象
     * @param splitMode 分段方式
     * @param maxChunkSize 对于字符长度分割：最大分段长度；对于智能章节分割：最大章节长度
     * @param overlapSize 对于字符长度分割：重叠长度；对于智能章节分割：最小章节长度
     * @return 分段列表
     */
    public List<DocumentSegment> loadAndSplitDocument(File file, SplitMode splitMode,
            int maxChunkSize, int overlapSize) {
        Document document = loadDocument(file);
        return splitDocumentBySplitMode(document, splitMode, maxChunkSize, overlapSize);
    }

    /**
     * 根据分段方式加载并分割文档（使用默认参数）
     * 
     * @param file 文件对象
     * @param splitMode 分段方式
     * @return 分段列表
     */
    public List<DocumentSegment> loadAndSplitDocument(File file, SplitMode splitMode) {
        Document document = loadDocument(file);
        return splitDocumentBySplitMode(document, splitMode);
    }

    /**
     * 根据分段方式分割文档
     * 
     * @param document 文档对象
     * @param splitMode 分段方式
     * @param maxChunkSize 对于字符长度分割：最大分段长度；对于智能章节分割：最大章节长度
     * @param overlapSize 对于字符长度分割：重叠长度；对于智能章节分割：最小章节长度
     * @return 分段列表
     */
    public List<DocumentSegment> splitDocumentBySplitMode(Document document, SplitMode splitMode,
            int maxChunkSize, int overlapSize) {
        switch (splitMode) {
            case CHARACTER_SPLITTER:
                return splitDocument(document, maxChunkSize, overlapSize);
            case CHAPTER_SPLITTER:
                // 对于智能章节分割，maxChunkSize作为最大章节长度，overlapSize作为最小章节长度
                return splitDocumentBySmartChapter(document, true, overlapSize, maxChunkSize);
            default:
                throw new IllegalArgumentException("不支持的分段方式: " + splitMode);
        }
    }

    /**
     * 根据分段方式分割文档（使用默认参数）
     * 
     * @param document 文档对象
     * @param splitMode 分段方式
     * @return 分段列表
     */
    public List<DocumentSegment> splitDocumentBySplitMode(Document document, SplitMode splitMode) {
        switch (splitMode) {
            case CHARACTER_SPLITTER:
                return splitDocument(document);
            case CHAPTER_SPLITTER:
                return splitDocumentBySmartChapter(document);
            default:
                throw new IllegalArgumentException("不支持的分段方式: " + splitMode);
        }
    }
}
