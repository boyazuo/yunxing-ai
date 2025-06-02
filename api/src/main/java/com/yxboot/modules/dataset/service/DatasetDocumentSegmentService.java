package com.yxboot.modules.dataset.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.llm.document.DocumentSegment;
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import com.yxboot.modules.dataset.mapper.DatasetDocumentSegmentMapper;
import lombok.RequiredArgsConstructor;

/**
 * 文档分段服务实现类
 * 
 * @author Boya
 */
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
     * 更新文档分段内容
     * 
     * @param segmentId 分段ID
     * @param content 新内容
     * @param title 新标题（可选）
     * @return 是否更新成功
     */
    public boolean updateSegmentContent(Long segmentId, String content, String title) {
        DatasetDocumentSegment segment = getById(segmentId);
        if (segment == null) {
            return false;
        }

        if (content != null) {
            segment.setContent(content);
            segment.setContentLength(content.length());
        }

        if (title != null) {
            segment.setTitle(title);
        }

        return updateById(segment);
    }

    /**
     * 删除文档的所有分段
     * 
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSegmentsByDocumentId(Long documentId) {
        LambdaQueryWrapper<DatasetDocumentSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatasetDocumentSegment::getDocumentId, documentId);
        boolean success = remove(queryWrapper);
        return success;
    }
}
