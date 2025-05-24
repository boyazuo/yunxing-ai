package com.yxboot.modules.dataset.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.llm.document.service.DocumentProcessorService;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.embedding.model.EmbeddingModelFactory;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingConfig;
import com.yxboot.llm.storage.VectorStore;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.service.ModelService;
import com.yxboot.modules.ai.service.ProviderService;
import com.yxboot.modules.dataset.entity.Dataset;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.system.entity.SysFile;
import com.yxboot.modules.system.service.SysFileService;
import com.yxboot.util.HttpClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据集文档异步处理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetDocumentAsyncService {

    private final DocumentProcessorService documentProcessorService;
    private final DatasetDocumentService datasetDocumentService;
    private final DatasetService datasetService;
    private final DatasetDocumentSegmentService documentSegmentService;
    private final SysFileService sysFileService;
    private final ModelService modelService;
    private final ProviderService providerService;
    private final EmbeddingModelFactory embeddingModelFactory;
    private final VectorStore vectorStore;

    /**
     * 异步处理文档
     *
     * @param documentId 文档ID
     * @return CompletableFuture<Boolean> 处理结果
     */
    @Async
    public CompletableFuture<Boolean> processDocumentAsync(Long documentId) {
        try {
            // 更新文档状态为处理中
            boolean updated = datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.PROCESSING);
            if (!updated) {
                log.error("更新文档状态失败, documentId: {}", documentId);
                return CompletableFuture.completedFuture(false);
            }

            // 1. 获取文档信息
            DatasetDocument document = datasetDocumentService.getById(documentId);
            if (document == null) {
                log.error("文档不存在, documentId: {}", documentId);
                return CompletableFuture.completedFuture(false);
            }

            // 2. 获取文件信息
            SysFile sysFile = sysFileService.getById(document.getFileId());
            if (sysFile == null) {
                log.error("文件不存在, fileId: {}", document.getFileId());
                datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.FAILED);
                return CompletableFuture.completedFuture(false);
            }

            // 3. 获取文件路径或URL
            String filePath = sysFile.getPath();
            String fileUrl = sysFile.getUrl();

            if ((filePath == null || filePath.trim().isEmpty()) && (fileUrl == null || fileUrl.trim().isEmpty())) {
                log.error("文件路径和URL均为空, fileId: {}", document.getFileId());
                datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.FAILED);
                return CompletableFuture.completedFuture(false);
            }

            // 4. 加载文档
            File file = null;
            List<DocumentSegment> segments = null;

            // 尝试从本地路径加载
            if (StringUtils.hasText(filePath)) {
                Path path = Paths.get(filePath);
                file = path.toFile();
                if (file.exists()) {
                    log.info("从本地路径加载文档: {}", filePath);
                    segments = loadDocumentFromFile(file, document);
                } else {
                    log.warn("本地文件不存在, path: {}, 尝试从URL加载", filePath);
                }
            }

            // 如果本地文件不存在或未成功处理，尝试从URL加载
            if ((segments == null || segments.isEmpty()) && StringUtils.hasText(fileUrl)) {
                log.info("从URL加载文档: {}", fileUrl);
                file = downloadFromUrl(fileUrl, sysFile.getFileName());
                if (file != null) {
                    segments = loadDocumentFromFile(file, document);
                    // 处理完成后删除临时文件
                    try {
                        Files.deleteIfExists(file.toPath());
                    } catch (IOException e) {
                        log.warn("删除临时文件失败: {}", file.getAbsolutePath(), e);
                    }
                }
            }

            if (segments == null || segments.isEmpty()) {
                log.error("文档加载失败, documentId: {}", documentId);
                datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.FAILED);
                return CompletableFuture.completedFuture(false);
            }

            // 6. 保存文档分段
            List<String> contents = new ArrayList<>();
            List<String> titles = new ArrayList<>();

            for (DocumentSegment segment : segments) {
                contents.add(segment.getContent());
                titles.add(segment.getTitle());
            }

            log.info("保存文档分段, documentId: {}, 分段数量: {}", documentId, segments.size());
            int savedCount = documentSegmentService.batchCreateSegments(documentId, contents, titles);

            // 7. 向量化处理
            log.info("开始向量化处理, documentId: {}, 分段数量: {}", documentId, segments.size());

            // 获取数据集信息
            Dataset dataset = datasetService.getById(document.getDatasetId());
            if (dataset == null) {
                log.error("数据集不存在, datasetId: {}", document.getDatasetId());
                datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.FAILED);
                return CompletableFuture.completedFuture(false);
            }

            // 获取嵌入模型和提供商
            EmbeddingModel embeddingModel = getEmbeddingModel(dataset.getEmbeddingModelId());
            if (embeddingModel == null) {
                log.error("获取嵌入模型失败, embeddingModelId: {}", dataset.getEmbeddingModelId());
                datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.FAILED);
                return CompletableFuture.completedFuture(false);
            }

            vectorizeSegments(document, segments, embeddingModel);

            // 8. 更新文档状态为已完成
            datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.COMPLETED);
            log.info("文档处理完成, documentId: {}, 分段数量: {}", documentId, savedCount);

            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("文档处理失败, documentId: {}", documentId, e);
            // 更新文档状态为失败
            datasetDocumentService.updateDocumentStatus(documentId, DocumentStatus.FAILED);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 根据嵌入模型ID获取嵌入模型实例
     * 
     * @param embeddingModelId 嵌入模型ID
     * @return 嵌入模型实例
     */
    private EmbeddingModel getEmbeddingModel(Long embeddingModelId) {
        try {
            // 获取模型信息
            Model model = modelService.getById(embeddingModelId);
            if (model == null) {
                log.error("模型不存在, modelId: {}", embeddingModelId);
                return null;
            }

            // 获取提供商信息
            Provider provider = providerService.getProviderByModelId(embeddingModelId);
            if (provider == null) {
                log.error("提供商不存在, modelId: {}", embeddingModelId);
                return null;
            }

            // 使用工厂创建嵌入模型实例
            EmbeddingModel embeddingModel = embeddingModelFactory.createEmbeddingModel(provider);

            // 配置模型
            ZhipuAIEmbeddingConfig config = ZhipuAIEmbeddingConfig.builder()
                    .apiKey(provider.getApiKey())
                    .modelName(model.getModelName())
                    .build();

            // if (provider.getEndpoint() != null && !provider.getEndpoint().isEmpty()) {
            // config.setBaseUrl(provider.getEndpoint());
            // }

            embeddingModel.configure(config);

            return embeddingModel;
        } catch (Exception e) {
            log.error("获取嵌入模型失败, embeddingModelId: {}", embeddingModelId, e);
            return null;
        }
    }

    /**
     * 从文件加载并分割文档
     * 
     * @param file     文件对象
     * @param document 文档对象
     * @return 分段列表
     */
    private List<DocumentSegment> loadDocumentFromFile(File file, DatasetDocument document) {
        if (file == null || !file.exists()) {
            return null;
        }

        log.info("开始处理文档, documentId: {}, filePath: {}", document.getDocumentId(), file.getAbsolutePath());
        return documentProcessorService.loadAndSplitDocument(
                file,
                document.getMaxSegmentLength(),
                document.getOverlapLength());
    }

    /**
     * 从URL下载文件到临时目录
     * 
     * @param fileUrl  文件URL
     * @param fileName 文件名
     * @return 下载的临时文件
     */
    private File downloadFromUrl(String fileUrl, String fileName) {
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

    /**
     * 向量化文档分段
     *
     * @param document       文档
     * @param segments       分段列表
     * @param embeddingModel 嵌入模型实例
     */
    private void vectorizeSegments(DatasetDocument document, List<DocumentSegment> segments,
            EmbeddingModel embeddingModel) {
        log.info("开始向量化, documentId: {}, 分段数量: {}, 使用模型: {}",
                document.getDocumentId(), segments.size(), embeddingModel.getModelName());

        try {
            List<String> ids = new ArrayList<>();
            List<float[]> vectors = new ArrayList<>();
            List<Map<String, Object>> metadataList = new ArrayList<>();
            List<String> texts = new ArrayList<>();

            // 批量处理，每次最多处理20个分段
            int batchSize = 20;
            for (int i = 0; i < segments.size(); i += batchSize) {
                int end = Math.min(i + batchSize, segments.size());
                List<DocumentSegment> batch = segments.subList(i, end);

                // 准备批量向量化的数据
                List<String> batchTexts = new ArrayList<>();
                for (DocumentSegment segment : batch) {
                    batchTexts.add(segment.getContent());
                }

                // 批量生成向量
                List<float[]> batchVectors = embeddingModel.embedAll(batchTexts);

                // 准备存储数据
                for (int j = 0; j < batch.size(); j++) {
                    DocumentSegment segment = batch.get(j);

                    // 生成唯一ID
                    String vectorId = segment.getId();

                    // 元数据
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("document_id", document.getDocumentId());
                    metadata.put("dataset_id", document.getDatasetId());
                    metadata.put("tenant_id", document.getTenantId());
                    metadata.put("segment_index", i + j);
                    metadata.put("title", segment.getTitle());

                    ids.add(vectorId);
                    vectors.add(batchVectors.get(j));
                    metadataList.add(metadata);
                    texts.add(segment.getContent());
                }

                // 批量保存向量
                vectorStore.addVectors(ids, vectors, metadataList, texts);

                // 清空当前批次的数据，准备下一批次
                ids.clear();
                vectors.clear();
                metadataList.clear();
                texts.clear();

                log.info("向量化进度: {}/{}", end, segments.size());
            }

            log.info("向量化完成, documentId: {}", document.getDocumentId());
        } catch (Exception e) {
            log.error("向量化失败, documentId: {}", document.getDocumentId(), e);
            throw new RuntimeException("向量化失败: " + e.getMessage());
        }
    }
}