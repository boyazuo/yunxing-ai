package com.yxboot.modules.dataset.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yxboot.common.api.ResultCode;
import com.yxboot.common.exception.ApiException;
import com.yxboot.llm.client.vector.VectorStoreClient;
import com.yxboot.modules.dataset.dto.DatasetDocumentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.dataset.enums.SegmentMethod;
import com.yxboot.modules.dataset.service.DatasetDocumentSegmentService;
import com.yxboot.modules.dataset.service.DatasetDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档应用服务 负责文档相关的业务流程编排，协调多个领域服务
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetDocumentApplicationService {

    private final DatasetDocumentService datasetDocumentService;
    private final DatasetDocumentSegmentService segmentService;
    private final VectorStoreClient vectorService;

    /**
     * 创建文档记录 注意：异步处理需要在Controller层单独调用DatasetDocumentProcessingApplicationService
     * 
     * @param tenantId 租户ID
     * @param datasetId 知识库ID
     * @param fileId 文件ID
     * @param fileName 文件名称
     * @param fileSize 文件大小
     * @param fileHash 文件hash值
     * @param segmentMethod 分段方式
     * @param maxSegmentLength 分段最大长度
     * @param overlapLength 重叠长度
     * @return 文档对象
     */
    @Transactional
    public DatasetDocument createDocument(Long tenantId, Long datasetId, Long fileId, String fileName, Integer fileSize,
            String fileHash,
            SegmentMethod segmentMethod, Integer maxSegmentLength, Integer overlapLength) {

        log.info("开始创建文档, fileName: {}, datasetId: {}", fileName, datasetId);

        // 1. 检查文档是否已存在
        DatasetDocument existingDocument =
                datasetDocumentService.checkDocumentExistsByHash(tenantId, datasetId, fileHash);
        if (existingDocument != null) {
            log.warn("文档已存在, fileHash: {}, documentId: {}", fileHash, existingDocument.getDocumentId());
            throw new ApiException(ResultCode.VALIDATE_FAILED, "该文档已存在于知识库中，文档名称：" + existingDocument.getFileName());
        }

        // 2. 创建文档记录
        DatasetDocument document = datasetDocumentService.createDocument(tenantId, datasetId, fileId, fileName,
                fileSize, fileHash, segmentMethod,
                maxSegmentLength, overlapLength);

        log.info("文档创建成功, documentId: {}", document.getDocumentId());
        return document;
    }

    /**
     * 删除文档及其所有关联数据 协调删除向量、分段和文档本身
     * 
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDocumentCompletely(Long documentId) {
        log.info("删除文档及关联数据, documentId: {}", documentId);

        try {
            // 1. 获取文档信息
            DatasetDocument document = datasetDocumentService.getById(documentId);
            if (document == null) {
                log.warn("文档不存在, documentId: {}", documentId);
                return false;
            }

            // 2. 删除文档的向量数据
            vectorService.deleteDocumentVectors(documentId, document.getDatasetId());

            // 3. 删除文档分段
            segmentService.deleteSegmentsByDocumentId(documentId);

            // 4. 删除文档记录
            datasetDocumentService.deleteDocument(documentId);

            log.info("文档删除成功, documentId: {}", documentId);
            return true;
        } catch (Exception e) {
            log.error("删除文档失败, documentId: {}", documentId, e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }

    /**
     * 批量删除文档及其关联数据
     * 
     * @param documentIds 文档ID列表
     * @return 删除成功的文档数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteDocuments(List<Long> documentIds) {
        log.info("批量删除文档, count: {}", documentIds.size());

        int successCount = 0;
        for (Long documentId : documentIds) {
            try {
                if (deleteDocumentCompletely(documentId)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("删除文档失败, documentId: {}", documentId, e);
            }
        }

        log.info("批量删除文档完成, 成功: {}, 总数: {}", successCount, documentIds.size());
        return successCount;
    }

    /**
     * 更新文档状态
     * 
     * @param documentId 文档ID
     * @param status 新状态
     * @return 是否更新成功
     */
    public boolean updateDocumentStatus(Long documentId, DocumentStatus status) {
        return datasetDocumentService.updateDocumentStatus(documentId, status);
    }

    /**
     * 分页查询知识库下的文档
     * 
     * @param datasetId 知识库ID
     * @param current 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public IPage<DatasetDocumentDTO> getDocumentsByDatasetId(Long datasetId, long current, long size) {
        return datasetDocumentService.getDocumentsByDatasetId(datasetId, current, size);
    }

    /**
     * 获取知识库下的所有文档
     * 
     * @param datasetId 知识库ID
     * @return 文档列表
     */
    public List<DatasetDocumentDTO> listDocumentsByDatasetId(Long datasetId) {
        return datasetDocumentService.listDocumentsByDatasetId(datasetId);
    }

    /**
     * 获取租户下的所有文档
     * 
     * @param tenantId 租户ID
     * @return 文档列表
     */
    public List<DatasetDocumentDTO> listDocumentsByTenantId(Long tenantId) {
        return datasetDocumentService.listDocumentsByTenantId(tenantId);
    }

    /**
     * 根据ID获取文档详情
     * 
     * @param documentId 文档ID
     * @return 文档对象
     */
    public DatasetDocument getDocumentById(Long documentId) {
        return datasetDocumentService.getById(documentId);
    }

    /**
     * 更新文档分段数
     * 
     * @param documentId 文档ID
     * @param segmentNum 分段数
     * @return 是否更新成功
     */
    public boolean updateDocumentSegmentNum(Long documentId, Integer segmentNum) {
        return datasetDocumentService.updateDocumentSegmentNum(documentId, segmentNum);
    }
}
