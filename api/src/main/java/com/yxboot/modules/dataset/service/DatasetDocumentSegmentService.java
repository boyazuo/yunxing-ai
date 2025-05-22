package com.yxboot.modules.dataset.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    private final DatasetDocumentService datasetDocumentService;

    /**
     * 批量创建文档分段
     * 
     * @param documentId    文档ID
     * @param segments      分段内容列表
     * @param segmentTitles 分段标题列表（可选）
     * @return 创建的分段数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchCreateSegments(Long documentId, List<String> segments, List<String> segmentTitles) {
        if (segments == null || segments.isEmpty()) {
            return 0;
        }

        // 获取文档信息
        DatasetDocument document = datasetDocumentService.getById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在");
        }

        Long tenantId = document.getTenantId();
        Long datasetId = document.getDatasetId();

        // 删除现有的分段
        LambdaQueryWrapper<DatasetDocumentSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatasetDocumentSegment::getDocumentId, documentId);
        remove(queryWrapper);

        // 批量插入新分段
        List<DatasetDocumentSegment> segmentList = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            String content = segments.get(i);
            if (content == null || content.trim().isEmpty()) {
                continue;
            }

            DatasetDocumentSegment segment = new DatasetDocumentSegment();
            segment.setTenantId(tenantId);
            segment.setDatasetId(datasetId);
            segment.setDocumentId(documentId);
            segment.setPosition(i);
            segment.setContent(content);
            segment.setContentLength(content.length());

            // 设置标题（如果提供）
            if (segmentTitles != null && i < segmentTitles.size()) {
                segment.setTitle(segmentTitles.get(i));
            }

            segmentList.add(segment);
        }

        // 批量保存
        saveBatch(segmentList);

        // 更新文档的分段数
        document.setSegmentNum(segmentList.size());
        datasetDocumentService.updateById(document);

        return segmentList.size();
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
     * @param page       页码
     * @param size       每页大小
     * @param documentId 文档ID
     * @return 分页结果
     */
    public IPage<DatasetDocumentSegmentDTO> pageSegmentsByDocumentId(int page, int size, Long documentId) {
        Page<DatasetDocumentSegmentDTO> pageParam = new Page<>(page, size);
        return baseMapper.pageSegmentsByDocumentId(pageParam, documentId);
    }

    /**
     * 根据数据集ID获取所有分段
     * 
     * @param datasetId 数据集ID
     * @return 分段列表
     */
    public List<DatasetDocumentSegmentDTO> getSegmentsByDatasetId(Long datasetId) {
        return baseMapper.getSegmentsByDatasetId(datasetId);
    }

    /**
     * 更新文档分段内容
     * 
     * @param segmentId 分段ID
     * @param content   新内容
     * @param title     新标题（可选）
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

        // 更新文档的分段数为0
        if (success) {
            DatasetDocument document = datasetDocumentService.getById(documentId);
            if (document != null) {
                document.setSegmentNum(0);
                datasetDocumentService.updateById(document);
            }
        }

        return success;
    }
}