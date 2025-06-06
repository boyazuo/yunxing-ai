package com.yxboot.modules.dataset.application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.llm.document.service.DocumentProcessorService;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.dataset.infrastructure.VectorServiceImpl;
import com.yxboot.modules.dataset.service.DatasetDocumentSegmentService;
import com.yxboot.modules.dataset.service.DatasetDocumentService;
import com.yxboot.modules.system.entity.SysFile;
import com.yxboot.modules.system.service.SysFileService;
import com.yxboot.util.HttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档处理应用服务 负责文档异步处理的业务流程编排
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetDocumentProcessingApplicationService {

    private final DatasetDocumentService datasetDocumentService;
    private final DatasetDocumentSegmentService segmentService;
    private final SysFileService sysFileService;
    private final DocumentProcessorService documentProcessorService;
    private final VectorServiceImpl vectorService;

    /**
     * 异步处理文档 协调文档解析、分段创建、向量化等完整流程
     *
     * @param documentId 文档ID
     * @param userId 用户ID（为避免SecurityContext在异步线程中丢失，显式传递）
     * @return CompletableFuture<Boolean> 处理结果
     */
    @Async("documentTaskExecutor")
    public CompletableFuture<Boolean> processDocumentAsync(Long documentId, Long userId) {
        log.info("开始异步处理文档, documentId: {}, userId: {}", documentId, userId);

        try {
            // 1. 更新文档状态为处理中
            boolean updated = datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.PROCESSING);
            if (!updated) {
                log.error("更新文档状态失败, documentId: {}", documentId);
                return CompletableFuture.completedFuture(false);
            }

            // 2. 获取并验证文档信息
            DatasetDocument document = datasetDocumentService.getById(documentId);
            if (document == null) {
                log.error("文档不存在, documentId: {}", documentId);
                return CompletableFuture.completedFuture(false);
            }

            // 3. 获取并验证文件信息
            SysFile sysFile = sysFileService.getById(document.getFileId());
            if (sysFile == null) {
                log.error("文件不存在, fileId: {}", document.getFileId());
                updateDocumentStatusToFailed(documentId);
                return CompletableFuture.completedFuture(false);
            }

            // 4. 处理文档（解析和分段）
            List<DocumentSegment> segments = processDocumentFile(sysFile, document);
            if (segments == null || segments.isEmpty()) {
                log.error("文档处理失败, documentId: {}", documentId);
                updateDocumentStatusToFailed(documentId);
                return CompletableFuture.completedFuture(false);
            }

            // 5. 保存文档分段
            log.info("保存文档分段, documentId: {}, 分段数量: {}", documentId, segments.size());
            List<DatasetDocumentSegment> savedSegments = segmentService.batchCreateSegments(document, segments);

            // 6. 更新文档的分段数
            document.setSegmentNum(savedSegments.size());
            datasetDocumentService.updateById(document);

            // 7. 向量化处理
            log.info("开始向量化处理, documentId: {}, 分段数量: {}", documentId, savedSegments.size());
            int vectorizedCount = vectorService.batchCreateSegmentVectors(savedSegments, document.getDatasetId());

            if (vectorizedCount != savedSegments.size()) {
                log.warn("向量化部分失败, documentId: {}, 成功: {}, 总数: {}", documentId, vectorizedCount, savedSegments.size());
            }

            // 8. 更新文档状态为已完成
            datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.COMPLETED);
            log.info("文档处理完成, documentId: {}, 分段数量: {}", documentId, savedSegments.size());

            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            log.error("文档处理失败, documentId: {}", documentId, e);
            updateDocumentStatusToFailed(documentId);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 处理文档文件（解析和分段）
     * 
     * @param sysFile 系统文件信息
     * @param document 文档对象
     * @return 分段列表
     */
    private List<DocumentSegment> processDocumentFile(SysFile sysFile, DatasetDocument document) {
        String filePath = sysFile.getPath();
        String fileUrl = sysFile.getUrl();

        if ((filePath == null || filePath.trim().isEmpty()) && (fileUrl == null || fileUrl.trim().isEmpty())) {
            log.error("文件路径和URL均为空, fileId: {}", document.getFileId());
            return null;
        }

        File file = null;
        List<DocumentSegment> segments = null;

        // 尝试从本地路径加载
        if (StringUtils.hasText(filePath)) {
            Path path = Paths.get(filePath);
            file = path.toFile();
            if (file.exists()) {
                log.info("从本地路径加载文档: {}", filePath);
                segments = loadAndSplitDocument(file, document.getMaxSegmentLength(), document.getOverlapLength());
            } else {
                log.warn("本地文件不存在, path: {}, 尝试从URL加载", filePath);
            }
        }

        // 如果本地文件不存在或未成功处理，尝试从URL加载
        if ((segments == null || segments.isEmpty()) && StringUtils.hasText(fileUrl)) {
            log.info("从URL加载文档: {}", fileUrl);
            file = downloadFromUrl(fileUrl, sysFile.getFileName());
            if (file != null) {
                segments = loadAndSplitDocument(file, document.getMaxSegmentLength(), document.getOverlapLength());

                // 处理完成后删除临时文件
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException e) {
                    log.warn("删除临时文件失败: {}", file.getAbsolutePath(), e);
                }
            }
        }

        return segments;
    }

    /**
     * 更新文档状态为失败
     * 
     * @param documentId 文档ID
     */
    private void updateDocumentStatusToFailed(Long documentId) {
        try {
            datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.FAILED);
        } catch (Exception e) {
            log.error("更新文档状态为失败时出错, documentId: {}", documentId, e);
        }
    }

    /**
     * 加载并分割文档
     * 
     * @param file 文件对象
     * @param maxSegmentLength 最大分段长度
     * @param overlapLength 重叠长度
     * @return 分段列表
     */
    private List<DocumentSegment> loadAndSplitDocument(File file, Integer maxSegmentLength, Integer overlapLength) {
        if (file == null || !file.exists()) {
            log.error("文件不存在或为空: {}", file != null ? file.getAbsolutePath() : "null");
            return null;
        }

        try {
            log.info("开始处理文档, filePath: {}, maxSegmentLength: {}, overlapLength: {}", file.getAbsolutePath(), maxSegmentLength, overlapLength);

            List<DocumentSegment> segments = documentProcessorService.loadAndSplitDocument(file, maxSegmentLength, overlapLength);

            log.info("文档处理完成, filePath: {}, 分段数量: {}", file.getAbsolutePath(), segments != null ? segments.size() : 0);

            return segments;
        } catch (Exception e) {
            log.error("文档处理失败, filePath: {}", file.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * 从URL下载文件到临时目录
     * 
     * @param fileUrl 文件URL
     * @param fileName 文件名
     * @return 下载的临时文件
     */
    private File downloadFromUrl(String fileUrl, String fileName) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            log.error("文件URL为空");
            return null;
        }

        try {
            // 创建临时目录和临时文件
            Path tempDir = Files.createTempDirectory("document_download_");
            File tempFile = tempDir.resolve(fileName).toFile();

            // 使用HttpClient下载文件
            log.info("开始从URL下载文件: {}", fileUrl);
            boolean success = HttpClient.downloadFile(fileUrl, tempFile);

            if (success && tempFile.exists() && tempFile.length() > 0) {
                log.info("文件下载成功: {}, 大小: {} bytes", tempFile.getAbsolutePath(), tempFile.length());
                return tempFile;
            } else {
                log.error("文件下载失败或文件为空: {}", tempFile.getAbsolutePath());
                return null;
            }
        } catch (Exception e) {
            log.error("从URL下载文件失败: {}", fileUrl, e);
            return null;
        }
    }
}
