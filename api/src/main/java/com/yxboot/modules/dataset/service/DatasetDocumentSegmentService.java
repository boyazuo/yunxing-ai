package com.yxboot.modules.dataset.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.ai.document.DocumentSegment;
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import com.yxboot.modules.dataset.mapper.DatasetDocumentSegmentMapper;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.yxboot.modules.account.entity.table.UserTableDef.USER;
import static com.yxboot.modules.dataset.entity.table.DatasetDocumentSegmentTableDef.DATASET_DOCUMENT_SEGMENT;
import static com.yxboot.modules.dataset.entity.table.DatasetDocumentTableDef.DATASET_DOCUMENT;
import static com.yxboot.modules.dataset.entity.table.DatasetTableDef.DATASET;

/**
 * 文档分段服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetDocumentSegmentService extends ServiceImpl<DatasetDocumentSegmentMapper, DatasetDocumentSegment> {

    @Transactional(rollbackFor = Exception.class)
    public List<DatasetDocumentSegment> batchCreateSegments(DatasetDocument document, List<DocumentSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return new ArrayList<>();
        }

        Long tenantId = document.getTenantId();
        Long datasetId = document.getDatasetId();

        List<DatasetDocumentSegment> segmentList = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            DocumentSegment ds = segments.get(i);
            String content = ds.getContent();
            if (content == null || content.trim().isEmpty()) {
                continue;
            }

            DatasetDocumentSegment segment = new DatasetDocumentSegment();
            segment.setTenantId(tenantId);
            segment.setDatasetId(datasetId);
            segment.setDocumentId(document.getDocumentId());
            segment.setVectorId(ds.getId());
            segment.setPosition(i);
            segment.setTitle(ds.getTitle());
            segment.setContent(content);
            segment.setContentLength(content.length());

            segmentList.add(segment);
        }

        saveBatch(segmentList);
        return segmentList;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateVectorIds(List<DatasetDocumentSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return true;
        }
        return updateBatch(segments);
    }

    public List<DatasetDocumentSegmentDTO> getSegmentsByDocumentId(Long documentId) {
        QueryWrapper wrapper = buildSegmentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT_SEGMENT.DOCUMENT_ID.eq(documentId));
        wrapper.orderBy(DATASET_DOCUMENT_SEGMENT.POSITION, true);
        return listAs(wrapper, DatasetDocumentSegmentDTO.class);
    }

    public Page<DatasetDocumentSegmentDTO> pageSegmentsByDocumentId(int page, int size, Long documentId) {
        QueryWrapper wrapper = buildSegmentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT_SEGMENT.DOCUMENT_ID.eq(documentId));
        wrapper.orderBy(DATASET_DOCUMENT_SEGMENT.POSITION, true);
        return pageAs(Page.of(page, size), wrapper, DatasetDocumentSegmentDTO.class);
    }

    public Page<DatasetDocumentSegmentDTO> pageSegmentsWithSearch(long current, long size, Long documentId,
            String keyword) {
        QueryWrapper wrapper = buildSegmentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT_SEGMENT.DOCUMENT_ID.eq(documentId));
        if (StrUtil.isNotEmpty(keyword)) {
            wrapper.where(DATASET_DOCUMENT_SEGMENT.TITLE.like(keyword)
                    .or(DATASET_DOCUMENT_SEGMENT.CONTENT.like(keyword)));
        }
        wrapper.orderBy(DATASET_DOCUMENT_SEGMENT.POSITION, true);
        return pageAs(Page.of(current, size), wrapper, DatasetDocumentSegmentDTO.class);
    }

    public List<DatasetDocumentSegmentDTO> getSegmentsByDatasetId(Long datasetId) {
        QueryWrapper wrapper = buildSegmentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT_SEGMENT.DATASET_ID.eq(datasetId));
        wrapper.orderBy(DATASET_DOCUMENT_SEGMENT.DOCUMENT_ID, true);
        wrapper.orderBy(DATASET_DOCUMENT_SEGMENT.POSITION, true);
        return listAs(wrapper, DatasetDocumentSegmentDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateSegmentContent(Long segmentId, String content, String title) {
        DatasetDocumentSegment segment = getById(segmentId);
        if (segment == null) {
            log.warn("分段不存在, segmentId: {}", segmentId);
            return false;
        }

        boolean contentChanged = false;
        if (content != null && !content.equals(segment.getContent())) {
            segment.setContent(content);
            segment.setContentLength(content.length());
            contentChanged = true;
        }

        if (title != null && !title.equals(segment.getTitle())) {
            segment.setTitle(title);
            contentChanged = true;
        }

        if (!contentChanged) {
            log.info("分段内容未发生变化, segmentId: {}", segmentId);
            return true;
        }

        boolean success = updateById(segment);
        if (success) {
            log.info("分段内容更新成功, segmentId: {}", segmentId);
        } else {
            log.error("分段内容更新失败, segmentId: {}", segmentId);
        }

        return success;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSegment(Long segmentId) {
        DatasetDocumentSegment segment = getById(segmentId);
        if (segment == null) {
            log.warn("分段不存在, segmentId: {}", segmentId);
            return true;
        }

        removeById(segmentId);
        log.info("分段删除完成, segmentId: {}", segmentId);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteSegments(List<Long> segmentIds) {
        if (segmentIds == null || segmentIds.isEmpty()) {
            return true;
        }

        removeByIds(segmentIds);
        log.info("批量删除分段完成, count: {}", segmentIds.size());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSegmentsByDocumentId(Long documentId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(DATASET_DOCUMENT_SEGMENT.DOCUMENT_ID.eq(documentId));
        remove(wrapper);
        log.info("删除文档分段完成, documentId: {}", documentId);
        return true;
    }

    private QueryWrapper buildSegmentDtoQueryWrapper() {
        var creator = USER.as("cu");
        var updator = USER.as("uu");

        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.select(DATASET_DOCUMENT_SEGMENT.ALL_COLUMNS);
        wrapper.select(creator.USERNAME.as("creatorUsername"));
        wrapper.select(creator.AVATAR.as("creatorAvatar"));
        wrapper.select(updator.USERNAME.as("updatorUsername"));
        wrapper.select(DATASET_DOCUMENT.FILE_NAME.as("documentName"));
        wrapper.select(DATASET.DATASET_NAME);
        wrapper.from(DATASET_DOCUMENT_SEGMENT);
        wrapper.leftJoin(creator).on(DATASET_DOCUMENT_SEGMENT.CREATOR_ID.eq(creator.USER_ID));
        wrapper.leftJoin(updator).on(DATASET_DOCUMENT_SEGMENT.UPDATOR_ID.eq(updator.USER_ID));
        wrapper.leftJoin(DATASET_DOCUMENT).on(DATASET_DOCUMENT_SEGMENT.DOCUMENT_ID.eq(DATASET_DOCUMENT.DOCUMENT_ID));
        wrapper.leftJoin(DATASET).on(DATASET_DOCUMENT_SEGMENT.DATASET_ID.eq(DATASET.DATASET_ID));
        return wrapper;
    }
}
