package com.yxboot.modules.dataset.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.dataset.dto.DatasetDocumentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.dataset.enums.SegmentMethod;
import com.yxboot.modules.dataset.mapper.DatasetDocumentMapper;

import lombok.RequiredArgsConstructor;

/**
 * 知识库文档服务实现类
 * 
 * @author Boya
 */
@Service
@RequiredArgsConstructor
public class DatasetDocumentService extends ServiceImpl<DatasetDocumentMapper, DatasetDocument> {

    /**
     * 检查指定知识库中是否已存在相同hash的文档
     * 
     * @param tenantId  租户ID
     * @param datasetId 知识库ID
     * @param fileHash  文件hash值
     * @return 存在的文档，如果不存在则返回null
     */
    public DatasetDocument checkDocumentExistsByHash(Long tenantId, Long datasetId, String fileHash) {
        if (fileHash == null || fileHash.trim().isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<DatasetDocument> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatasetDocument::getTenantId, tenantId)
                .eq(DatasetDocument::getDatasetId, datasetId)
                .eq(DatasetDocument::getFileHash, fileHash);

        return getOne(queryWrapper);
    }

    /**
     * 创建文档
     * 
     * @param tenantId         租户ID
     * @param datasetId        知识库ID
     * @param fileId           文件ID
     * @param fileName         文件名称
     * @param fileSize         文件大小
     * @param fileHash         文件hash值
     * @param segmentMethod    分段方式
     * @param maxSegmentLength 分段最大长度
     * @param overlapLength    重叠长度
     * @return 文档对象
     */
    @Transactional
    public DatasetDocument createDocument(
            Long tenantId,
            Long datasetId,
            Long fileId,
            String fileName,
            Integer fileSize,
            String fileHash,
            SegmentMethod segmentMethod,
            Integer maxSegmentLength,
            Integer overlapLength) {

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
     * @param current   页码
     * @param size      每页大小
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
     * @param status     新状态
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
}