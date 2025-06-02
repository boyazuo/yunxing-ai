package com.yxboot.modules.dataset.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.llm.storage.VectorStore;
import com.yxboot.modules.dataset.dto.DatasetDocumentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.dataset.enums.SegmentMethod;
import com.yxboot.modules.dataset.mapper.DatasetDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库文档服务实现类
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetDocumentService extends ServiceImpl<DatasetDocumentMapper, DatasetDocument> {

    private final DatasetDocumentSegmentService datasetDocumentSegmentService;
    private final VectorStore vectorStore;

    /**
     * 检查指定知识库中是否已存在相同hash的文档
     * 
     * @param tenantId 租户ID
     * @param datasetId 知识库ID
     * @param fileHash 文件hash值
     * @return 存在的文档，如果不存在则返回null
     */
    public DatasetDocument checkDocumentExistsByHash(Long tenantId, Long datasetId, String fileHash) {
        if (fileHash == null || fileHash.trim().isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<DatasetDocument> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatasetDocument::getTenantId, tenantId).eq(DatasetDocument::getDatasetId, datasetId).eq(DatasetDocument::getFileHash,
                fileHash);

        return getOne(queryWrapper, false);
    }

    /**
     * 创建文档
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
    public DatasetDocument createDocument(Long tenantId, Long datasetId, Long fileId, String fileName, Integer fileSize, String fileHash,
            SegmentMethod segmentMethod, Integer maxSegmentLength, Integer overlapLength) {

        DatasetDocument document = new DatasetDocument();
        document.setTenantId(tenantId);
        document.setDatasetId(datasetId);
        document.setFileId(fileId);
        document.setFileName(fileName);
        document.setFileSize(fileSize);
        document.setFileHash(fileHash);
        document.setSegmentMethod(segmentMethod);
        document.setMaxSegmentLength(maxSegmentLength);
        document.setOverlapLength(overlapLength);
        document.setSegmentNum(0); // 初始化分段数为0
        document.setStatus(DocumentStatus.PENDING); // 设置初始状态为待处理

        save(document);
        return document;
    }

    /**
     * 分页获取知识库下的文档
     * 
     * @param datasetId 知识库ID
     * @param current 页码
     * @param size 每页大小
     * @return 分页数据
     */
    public IPage<DatasetDocumentDTO> getDocumentsByDatasetId(Long datasetId, long current, long size) {
        Page<DatasetDocumentDTO> pageable = new Page<>(current, size);
        return baseMapper.getDocumentsByDatasetId(pageable, datasetId);
    }

    /**
     * 获取知识库下的所有文档
     * 
     * @param datasetId 知识库ID
     * @return 文档列表
     */
    public List<DatasetDocumentDTO> listDocumentsByDatasetId(Long datasetId) {
        return baseMapper.listDocumentsByDatasetId(datasetId);
    }

    /**
     * 获取租户下的所有文档
     * 
     * @param tenantId 租户ID
     * @return 文档列表
     */
    public List<DatasetDocumentDTO> listDocumentsByTenantId(Long tenantId) {
        return baseMapper.listDocumentsByTenantId(tenantId);
    }

    /**
     * 更新文档状态
     * 
     * @param documentId 文档ID
     * @param status 新状态
     * @return 是否成功
     */
    public boolean updateDocumentStatus(Long documentId, DocumentStatus status) {
        DatasetDocument document = getById(documentId);
        if (document == null) {
            return false;
        }
        document.setStatus(status);
        return updateById(document);
    }

    /**
     * 更新文档分段数
     * 
     * @param documentId 文档ID
     * @param segmentNum 分段数
     * @return 是否成功
     */
    public boolean updateDocumentSegmentNum(Long documentId, Integer segmentNum) {
        DatasetDocument document = getById(documentId);
        if (document == null) {
            return false;
        }
        document.setSegmentNum(segmentNum);
        if (segmentNum > 0 && document.getStatus() == DocumentStatus.PROCESSING) {
            document.setStatus(DocumentStatus.COMPLETED);
        }
        return updateById(document);
    }

    /**
     * 删除文档及其相关数据（包括向量数据）
     * 
     * @param documentId 文档ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDocumentWithVectors(Long documentId) {
        try {
            // 获取文档信息
            DatasetDocument document = getById(documentId);
            if (document == null) {
                log.warn("文档不存在, documentId: {}", documentId);
                return false;
            }

            // 1. 删除向量数据
            String collectionName = "dataset_" + document.getDatasetId();
            Map<String, Object> filter = new HashMap<>();
            filter.put("document_id", documentId);

            try {
                int deletedVectors = vectorStore.deleteVectorsByFilter(collectionName, filter);
                log.info("删除文档向量数据成功, documentId: {}, 删除数量: {}", documentId, deletedVectors);
            } catch (Exception e) {
                log.error("删除文档向量数据失败, documentId: {}", documentId, e);
                // 向量删除失败不影响数据库删除，只记录日志
            }

            // 2. 删除文档分段
            boolean segmentsDeleted = datasetDocumentSegmentService.deleteSegmentsByDocumentId(documentId);
            if (!segmentsDeleted) {
                log.error("删除文档分段失败, documentId: {}", documentId);
                return false;
            }

            // 3. 删除文档记录
            boolean documentDeleted = removeById(documentId);
            if (!documentDeleted) {
                log.error("删除文档记录失败, documentId: {}", documentId);
                return false;
            }

            log.info("文档删除成功, documentId: {}", documentId);
            return true;
        } catch (Exception e) {
            log.error("删除文档失败, documentId: {}", documentId, e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }
}
