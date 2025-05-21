package com.yxboot.llm.document.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.yxboot.llm.document.Document;

/**
 * 文档加载器抽象基类
 */
public abstract class AbstractDocumentLoader implements DocumentLoader {

    @Override
    public Document load(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filename", file.getName());
            metadata.put("file_path", file.getAbsolutePath());
            metadata.put("file_size", file.length());
            metadata.put("file_extension", getFileExtension(file.getName()));

            return loadWithMetadata(inputStream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("加载文件失败：" + file.getAbsolutePath(), e);
        }
    }

    @Override
    public Document load(Path path) {
        return load(path.toFile());
    }

    @Override
    public Document load(InputStream inputStream) {
        return loadWithMetadata(inputStream, new HashMap<>());
    }

    @Override
    public Document load(InputStream inputStream, String filename) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", filename);
        metadata.put("file_extension", getFileExtension(filename));

        return loadWithMetadata(inputStream, metadata);
    }

    @Override
    public Document load(byte[] bytes, String filename) {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filename", filename);
            metadata.put("file_size", bytes.length);
            metadata.put("file_extension", getFileExtension(filename));

            return loadWithMetadata(inputStream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("加载字节数组失败：" + filename, e);
        }
    }

    /**
     * 从输入流加载文档并添加元数据
     *
     * @param inputStream 输入流
     * @param metadata    元数据
     * @return 文档对象
     */
    protected abstract Document loadWithMetadata(InputStream inputStream, Map<String, Object> metadata);

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    protected String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}