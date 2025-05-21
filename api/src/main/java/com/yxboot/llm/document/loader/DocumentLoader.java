package com.yxboot.llm.document.loader;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import com.yxboot.llm.document.Document;

/**
 * 文档加载器接口
 * 用于加载不同类型的文档
 */
public interface DocumentLoader {

    /**
     * 从文件加载文档
     *
     * @param file 文件对象
     * @return 文档对象
     */
    Document load(File file);

    /**
     * 从路径加载文档
     *
     * @param path 文件路径
     * @return 文档对象
     */
    Document load(Path path);

    /**
     * 从输入流加载文档
     *
     * @param inputStream 输入流
     * @return 文档对象
     */
    Document load(InputStream inputStream);

    /**
     * 从输入流加载文档
     *
     * @param inputStream 输入流
     * @param filename    文件名，用于识别文件类型
     * @return 文档对象
     */
    Document load(InputStream inputStream, String filename);

    /**
     * 从字节数组加载文档
     *
     * @param bytes    字节数组
     * @param filename 文件名，用于识别文件类型
     * @return 文档对象
     */
    Document load(byte[] bytes, String filename);
}