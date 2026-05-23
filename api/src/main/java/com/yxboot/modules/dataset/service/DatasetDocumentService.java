package com.yxboot.modules.dataset.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.dataset.dto.DatasetDocumentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.dataset.enums.SegmentMethod;
import com.yxboot.modules.dataset.mapper.DatasetDocumentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.yxboot.modules.account.entity.table.UserTableDef.USER;
import static com.yxboot.modules.dataset.entity.table.DatasetDocumentTableDef.DATASET_DOCUMENT;
import static com.yxboot.modules.dataset.entity.table.DatasetTableDef.DATASET;

/**
 * 知识库文档服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetDocumentService extends ServiceImpl<DatasetDocumentMapper, DatasetDocument> {

    public DatasetDocument checkDocumentExistsByHash(Long tenantId, Long datasetId, String fileHash) {
        if (fileHash == null || fileHash.trim().isEmpty()) {
            return null;
        }

        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(DATASET_DOCUMENT.TENANT_ID.eq(tenantId));
        wrapper.where(DATASET_DOCUMENT.DATASET_ID.eq(datasetId));
        wrapper.where(DATASET_DOCUMENT.FILE_HASH.eq(fileHash));

        return list(wrapper.limit(1)).stream().findFirst().orElse(null);
    }

    @Transactional
    public DatasetDocument createDocument(Long tenantId, Long datasetId, Long fileId, String fileName, Integer fileSize,
            String fileHash,
            SegmentMethod segmentMethod, Integer maxSegmentLength, Integer overlapLength, Integer parentChunkSize) {

        DatasetDocument document = new DatasetDocument();
        document.setTenantId(tenantId);
        document.setDatasetId(datasetId);
        document.setFileId(fileId);
        document.setFileName(fileName);
        document.setFileSize(fileSize);
        document.setFileHash(fileHash);
        document.setSegmentMethod(segmentMethod != null ? segmentMethod : SegmentMethod.PARENT_CHILD);
        document.setMaxSegmentLength(maxSegmentLength);
        document.setOverlapLength(overlapLength);
        document.setParentChunkSize(parentChunkSize != null ? parentChunkSize : 1200);
        document.setSegmentNum(0);
        document.setStatus(DocumentStatus.PENDING);

        save(document);
        return document;
    }

    public Page<DatasetDocumentDTO> getDocumentsByDatasetId(Long datasetId, long current, long size) {
        QueryWrapper wrapper = buildDocumentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT.DATASET_ID.eq(datasetId));
        wrapper.orderBy(DATASET_DOCUMENT.CREATE_TIME, false);
        return pageAs(Page.of(current, size), wrapper, DatasetDocumentDTO.class);
    }

    public List<DatasetDocumentDTO> listDocumentsByDatasetId(Long datasetId) {
        QueryWrapper wrapper = buildDocumentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT.DATASET_ID.eq(datasetId));
        wrapper.orderBy(DATASET_DOCUMENT.CREATE_TIME, false);
        return listAs(wrapper, DatasetDocumentDTO.class);
    }

    public List<DatasetDocumentDTO> listDocumentsByTenantId(Long tenantId) {
        QueryWrapper wrapper = buildDocumentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT.TENANT_ID.eq(tenantId));
        wrapper.orderBy(DATASET_DOCUMENT.CREATE_TIME, false);
        return listAs(wrapper, DatasetDocumentDTO.class);
    }

    public boolean updateDocumentStatus(Long documentId, DocumentStatus status) {
        DatasetDocument document = getById(documentId);
        if (document == null) {
            return false;
        }
        document.setStatus(status);
        return updateById(document);
    }

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

    public boolean deleteDocument(Long documentId) {
        DatasetDocument document = getById(documentId);
        if (document == null) {
            log.warn("文档不存在, documentId: {}", documentId);
            return true;
        }

        removeById(documentId);
        log.info("文档删除完成, documentId: {}", documentId);
        return true;
    }

    private QueryWrapper buildDocumentDtoQueryWrapper() {
        var creator = USER.as("cu");
        var updator = USER.as("uu");

        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.select(DATASET_DOCUMENT.ALL_COLUMNS);
        wrapper.select(DATASET.DATASET_NAME);
        wrapper.select(creator.USERNAME.as("creatorUsername"));
        wrapper.select(creator.AVATAR.as("creatorAvatar"));
        wrapper.select(updator.USERNAME.as("updatorUsername"));
        wrapper.from(DATASET_DOCUMENT);
        wrapper.leftJoin(DATASET).on(DATASET_DOCUMENT.DATASET_ID.eq(DATASET.DATASET_ID));
        wrapper.leftJoin(creator).on(DATASET_DOCUMENT.CREATOR_ID.eq(creator.USER_ID));
        wrapper.leftJoin(updator).on(DATASET_DOCUMENT.UPDATOR_ID.eq(updator.USER_ID));
        return wrapper;
    }
}
