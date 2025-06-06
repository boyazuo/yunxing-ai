package com.yxboot.modules.dataset.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import com.yxboot.modules.dataset.mapper.DatasetDocumentSegmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档分段服务实现类
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetDocumentSegmentService extends ServiceImpl<DatasetDocumentSegmentMapper, DatasetDocumentSegment> {

    /**
     * 批量创建文档分段
     * 
     * @param documentId 文档ID
     * @param segments 分段内容列表
     * @param segmentTitles 分段标题列表（可选）
     * @return 创建的分段列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<DatasetDocumentSegment> batchCreateSegments(DatasetDocument document, List<DocumentSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return new ArrayList<>();
        }

        Long tenantId = document.getTenantId();
        Long datasetId = document.getDatasetId();

        // 批量插入新分段
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

        // 批量保存
        saveBatch(segmentList);

        return segmentList;
    }

    /**
     * 批量更新分段的向量ID
     * 
     * @param segments 分段列表
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateVectorIds(List<DatasetDocumentSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return true;
        }

        return updateBatchById(segments);
    }

    /**
     * 获取文档的所有分段
     * 
     * @param documentId 文档ID
     * @return 分段列表
     */
    public List<DatasetDocumentSegmentDTO> getSegmentsByDocumentId(Long documentId) {
        return baseMapper.getSegmentsByDocumentId(documentId);
    }

    /**
     * 分页获取文档的分段
     * 
     * @param page 页码
     * @param size 每页大小
     * @param documentId 文档ID
     * @return 分页结果
     */
    public IPage<DatasetDocumentSegmentDTO> pageSegmentsByDocumentId(int page, int size, Long documentId) {
        Page<DatasetDocumentSegmentDTO> pageParam = new Page<>(page, size);
        return baseMapper.pageSegmentsByDocumentId(pageParam, documentId);
    }

    /**
     * 分页获取文档的分段（带搜索）
     * 
     * @param current 页码
     * @param size 每页大小
     * @param documentId 文档ID
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    public IPage<DatasetDocumentSegmentDTO> pageSegmentsWithSearch(long current, long size, Long documentId, String keyword) {
        Page<DatasetDocumentSegmentDTO> pageParam = new Page<>(current, size);
        return baseMapper.pageSegmentsWithSearch(pageParam, documentId, keyword);
    }

    /**
     * 根据知识库ID获取所有分段
     * 
     * @param datasetId 知识库ID
     * @return 分段列表
     */
    public List<DatasetDocumentSegmentDTO> getSegmentsByDatasetId(Long datasetId) {
        return baseMapper.getSegmentsByDatasetId(datasetId);
    }

    /**
     * 更新文档分段内容（仅更新数据库，不处理向量） 注意：向量的更新应该在应用服务层或基础设施层处理
     * 
     * @param segmentId 分段ID
     * @param content 新内容
     * @param title 新标题（可选）
     * @return 是否更新成功
     */
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

    /**
     * 删除分段（仅删除数据库记录，不处理向量） 注意：向量的删除应该在应用服务层或基础设施层处理
     * 
     * @param segmentId 分段ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSegment(Long segmentId) {
        DatasetDocumentSegment segment = getById(segmentId);
        if (segment == null) {
            log.warn("分段不存在, segmentId: {}", segmentId);
            return false;
        }

        boolean success = removeById(segmentId);
        if (success) {
            log.info("分段删除成功, segmentId: {}", segmentId);
        } else {
            log.error("分段删除失败, segmentId: {}", segmentId);
        }

        return success;
    }

    /**
     * 批量删除分段（仅删除数据库记录，不处理向量） 注意：向量的删除应该在应用服务层或基础设施层处理
     * 
     * @param segmentIds 分段ID列表
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteSegments(List<Long> segmentIds) {
        if (segmentIds == null || segmentIds.isEmpty()) {
            return true;
        }

        boolean success = removeByIds(segmentIds);
        if (success) {
            log.info("批量删除分段成功, count: {}", segmentIds.size());
        } else {
            log.error("批量删除分段失败, segmentIds: {}", segmentIds);
        }

        return success;
    }



    /**
     * 删除文档的所有分段
     * 
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSegmentsByDocumentId(Long documentId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DatasetDocumentSegment> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(DatasetDocumentSegment::getDocumentId, documentId);
        boolean success = remove(queryWrapper);
        return success;
    }
}
