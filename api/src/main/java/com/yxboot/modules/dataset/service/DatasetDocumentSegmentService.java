package com.yxboot.modules.dataset.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.ai.document.DocumentSegment;
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import com.yxboot.modules.dataset.enums.SegmentType;
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

        boolean hasParentChild = segments.stream()
                .anyMatch(s -> s.getSegmentType() == SegmentType.PARENT || s.getSegmentType() == SegmentType.CHILD);
        if (hasParentChild) {
            return batchCreateParentChildSegments(document, segments);
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

            DatasetDocumentSegment segment = buildSegmentEntity(document, ds, i, SegmentType.NORMAL, null);
            segmentList.add(segment);
        }

        saveBatch(segmentList);
        return segmentList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<DatasetDocumentSegment> batchCreateParentChildSegments(DatasetDocument document,
            List<DocumentSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return new ArrayList<>();
        }

        List<DocumentSegment> parents = segments.stream()
                .filter(s -> s.getSegmentType() == SegmentType.PARENT)
                .toList();
        List<DocumentSegment> children = segments.stream()
                .filter(s -> s.getSegmentType() == SegmentType.CHILD)
                .toList();
        List<DocumentSegment> normals = segments.stream()
                .filter(s -> s.getSegmentType() == SegmentType.NORMAL)
                .toList();

        Map<String, Long> parentUuidToSegmentId = new HashMap<>();
        List<DatasetDocumentSegment> allSaved = new ArrayList<>();
        int position = 0;

        List<DatasetDocumentSegment> parentEntities = new ArrayList<>();
        for (DocumentSegment ds : parents) {
            if (ds.getContent() == null || ds.getContent().trim().isEmpty()) {
                continue;
            }
            parentEntities.add(buildSegmentEntity(document, ds, position++, SegmentType.PARENT, null));
        }
        if (!parentEntities.isEmpty()) {
            saveBatch(parentEntities);
            int parentIndex = 0;
            for (DocumentSegment ds : parents) {
                if (ds.getContent() == null || ds.getContent().trim().isEmpty()) {
                    continue;
                }
                parentUuidToSegmentId.put(ds.getId(), parentEntities.get(parentIndex).getSegmentId());
                parentIndex++;
            }
            allSaved.addAll(parentEntities);
        }

        List<DatasetDocumentSegment> childEntities = new ArrayList<>();
        for (DocumentSegment ds : children) {
            if (ds.getContent() == null || ds.getContent().trim().isEmpty()) {
                continue;
            }
            Long parentSegmentId = parentUuidToSegmentId.get(ds.getParentId());
            childEntities.add(buildSegmentEntity(document, ds, position++, SegmentType.CHILD, parentSegmentId));
        }
        if (!childEntities.isEmpty()) {
            saveBatch(childEntities);
            allSaved.addAll(childEntities);
        }

        List<DatasetDocumentSegment> normalEntities = new ArrayList<>();
        for (DocumentSegment ds : normals) {
            if (ds.getContent() == null || ds.getContent().trim().isEmpty()) {
                continue;
            }
            normalEntities.add(buildSegmentEntity(document, ds, position++, SegmentType.NORMAL, null));
        }
        if (!normalEntities.isEmpty()) {
            saveBatch(normalEntities);
            allSaved.addAll(normalEntities);
        }

        return allSaved;
    }

    private DatasetDocumentSegment buildSegmentEntity(DatasetDocument document, DocumentSegment ds, int position,
            int segmentType, Long parentSegmentId) {
        String content = ds.getContent();
        DatasetDocumentSegment segment = new DatasetDocumentSegment();
        segment.setTenantId(document.getTenantId());
        segment.setDatasetId(document.getDatasetId());
        segment.setDocumentId(document.getDocumentId());
        segment.setVectorId(segmentType == SegmentType.PARENT ? null : ds.getId());
        segment.setPosition(position);
        segment.setSegmentType(segmentType);
        segment.setParentSegmentId(parentSegmentId);
        segment.setTitle(ds.getTitle());
        segment.setContent(content);
        segment.setContentLength(content.length());
        return segment;
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
        return pageSegmentsByDocumentId(page, size, documentId, "segments");
    }

    public Page<DatasetDocumentSegmentDTO> pageSegmentsByDocumentId(int page, int size, Long documentId, String view) {
        QueryWrapper wrapper = buildSegmentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT_SEGMENT.DOCUMENT_ID.eq(documentId));
        applySegmentViewFilter(wrapper, view);
        wrapper.orderBy(DATASET_DOCUMENT_SEGMENT.POSITION, true);
        return pageAs(Page.of(page, size), wrapper, DatasetDocumentSegmentDTO.class);
    }

    public Page<DatasetDocumentSegmentDTO> pageSegmentsWithSearch(long current, long size, Long documentId,
            String keyword) {
        return pageSegmentsWithSearch(current, size, documentId, keyword, "segments");
    }

    public Page<DatasetDocumentSegmentDTO> pageSegmentsWithSearch(long current, long size, Long documentId,
            String keyword, String view) {
        QueryWrapper wrapper = buildSegmentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT_SEGMENT.DOCUMENT_ID.eq(documentId));
        applySegmentViewFilter(wrapper, view);
        if (StrUtil.isNotEmpty(keyword)) {
            wrapper.where(DATASET_DOCUMENT_SEGMENT.TITLE.like(keyword)
                    .or(DATASET_DOCUMENT_SEGMENT.CONTENT.like(keyword)));
        }
        wrapper.orderBy(DATASET_DOCUMENT_SEGMENT.POSITION, true);
        return pageAs(Page.of(current, size), wrapper, DatasetDocumentSegmentDTO.class);
    }

    public List<DatasetDocumentSegmentDTO> listChildSegmentsByParentId(Long parentSegmentId) {
        QueryWrapper wrapper = buildSegmentDtoQueryWrapper();
        wrapper.where(DATASET_DOCUMENT_SEGMENT.PARENT_SEGMENT_ID.eq(parentSegmentId));
        wrapper.orderBy(DATASET_DOCUMENT_SEGMENT.POSITION, true);
        return listAs(wrapper, DatasetDocumentSegmentDTO.class);
    }

    public long countSegmentsByDocumentIdAndType(Long documentId, int segmentType) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(DATASET_DOCUMENT_SEGMENT.DOCUMENT_ID.eq(documentId));
        wrapper.where(DATASET_DOCUMENT_SEGMENT.SEGMENT_TYPE.eq(segmentType));
        return count(wrapper);
    }

    private void applySegmentViewFilter(QueryWrapper wrapper, String view) {
        if ("parents".equals(view)) {
            wrapper.where(DATASET_DOCUMENT_SEGMENT.SEGMENT_TYPE.eq(SegmentType.PARENT));
        } else {
            wrapper.where(DATASET_DOCUMENT_SEGMENT.SEGMENT_TYPE.in(SegmentType.NORMAL, SegmentType.CHILD));
        }
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
