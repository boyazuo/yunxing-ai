package com.yxboot.llm.document.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import com.yxboot.llm.document.Document;
import com.yxboot.llm.document.DocumentChunk;

/**
 * 文档处理服务接口
 */
public interface DocumentProcessorService {

    /**
     * 加载文档
     *
     * @param file 文件对象
     * @return 文档对象
     */
    Document loadDocument(File file);

    /**
     * 加载文档
     *
     * @param path 文件路径
     * @return 文档对象
     */
    Document loadDocument(Path path);

    /**
     * 加载文档
     *
     * @param inputStream 输入流
     * @param filename    文件名
     * @return 文档对象
     */
    Document loadDocument(InputStream inputStream, String filename);

    /**
     * 加载文档
     *
     * @param bytes    字节数组
     * @param filename 文件名
     * @return 文档对象
     */
    Document loadDocument(byte[] bytes, String filename);

    /**
     * 分割文档
     *
     * @param document 文档对象
     * @return 文档块列表
     */
    List<DocumentChunk> splitDocument(Document document);

    /**
     * 分割文档
     *
     * @param document     文档对象
     * @param maxChunkSize 最大块大小
     * @param overlapSize  重叠大小
     * @return 文档块列表
     */
    List<DocumentChunk> splitDocument(Document document, int maxChunkSize, int overlapSize);

    /**
     * 加载并分割文档（使用默认分割参数）
     *
     * @param file 文件对象
     * @return 文档块列表
     */
    List<DocumentChunk> loadAndSplitDocument(File file);

    /**
     * 加载并分割文档（使用默认分割参数）
     *
     * @param path 文件路径
     * @return 文档块列表
     */
    List<DocumentChunk> loadAndSplitDocument(Path path);

    /**
     * 加载并分割文档（使用默认分割参数）
     *
     * @param inputStream 输入流
     * @param filename    文件名
     * @return 文档块列表
     */
    List<DocumentChunk> loadAndSplitDocument(InputStream inputStream, String filename);

    /**
     * 加载并分割文档（使用自定义分割参数）
     *
     * @param file         文件对象
     * @param maxChunkSize 最大块大小
     * @param overlapSize  重叠大小
     * @return 文档块列表
     */
    List<DocumentChunk> loadAndSplitDocument(File file, int maxChunkSize, int overlapSize);
}