package com.yxboot.modules.dataset.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;

/**
 * 文档分段表 Mapper 接口
 * 
 * @author Boya
 */
public interface DatasetDocumentSegmentMapper extends BaseMapper<DatasetDocumentSegment> {

    /**
     * 获取文档的所有分段
     * 
     * @param documentId 文档ID
     * @return 分段列表
     */
    @Select("SELECT s.*, " +
            "cu.username as creator_username, " +
            "cu.avatar as creator_avatar, " +
            "uu.username as updator_username, " +
            "dd.file_name as document_name, " +
            "d.dataset_name " +
            "FROM dataset_document_segment s " +
            "LEFT JOIN user cu ON s.creator_id = cu.user_id " +
            "LEFT JOIN user uu ON s.updator_id = uu.user_id " +
            "LEFT JOIN dataset_document dd ON s.document_id = dd.document_id " +
            "LEFT JOIN dataset d ON s.dataset_id = d.dataset_id " +
            "WHERE s.document_id = #{documentId} " +
            "ORDER BY s.position ASC")
    List<DatasetDocumentSegmentDTO> getSegmentsByDocumentId(@Param("documentId") Long documentId);

    /**
     * 分页获取文档的分段
     * 
     * @param page       分页信息
     * @param documentId 文档ID
     * @return 分页结果
     */
    @Select("SELECT s.*, " +
            "cu.username as creator_username, " +
            "cu.avatar as creator_avatar, " +
            "uu.username as updator_username, " +
            "dd.file_name as document_name, " +
            "d.dataset_name " +
            "FROM dataset_document_segment s " +
            "LEFT JOIN user cu ON s.creator_id = cu.user_id " +
            "LEFT JOIN user uu ON s.updator_id = uu.user_id " +
            "LEFT JOIN dataset_document dd ON s.document_id = dd.document_id " +
            "LEFT JOIN dataset d ON s.dataset_id = d.dataset_id " +
            "WHERE s.document_id = #{documentId} " +
            "ORDER BY s.position ASC")
    IPage<DatasetDocumentSegmentDTO> pageSegmentsByDocumentId(Page<DatasetDocumentSegmentDTO> page,
            @Param("documentId") Long documentId);

    /**
     * 分页获取文档的分段（带搜索）
     * 
     * @param page       分页信息
     * @param documentId 文档ID
     * @param keyword    搜索关键词
     * @return 分页结果
     */
    @Select("SELECT s.*, " +
            "cu.username as creator_username, " +
            "cu.avatar as creator_avatar, " +
            "uu.username as updator_username, " +
            "dd.file_name as document_name, " +
            "d.dataset_name " +
            "FROM dataset_document_segment s " +
            "LEFT JOIN user cu ON s.creator_id = cu.user_id " +
            "LEFT JOIN user uu ON s.updator_id = uu.user_id " +
            "LEFT JOIN dataset_document dd ON s.document_id = dd.document_id " +
            "LEFT JOIN dataset d ON s.dataset_id = d.dataset_id " +
            "WHERE s.document_id = #{documentId} " +
            "AND (s.title LIKE CONCAT('%', #{keyword}, '%') OR s.content LIKE CONCAT('%', #{keyword}, '%')) "
            +
            "ORDER BY s.position ASC")
    IPage<DatasetDocumentSegmentDTO> pageSegmentsWithSearch(Page<DatasetDocumentSegmentDTO> page,
            @Param("documentId") Long documentId, @Param("keyword") String keyword);

    /**
     * 根据知识库ID获取所有分段
     * 
     * @param datasetId 知识库ID
     * @return 分段列表
     */
    @Select("SELECT s.*, " +
            "cu.username as creator_username, " +
            "cu.avatar as creator_avatar, " +
            "uu.username as updator_username, " +
            "dd.file_name as document_name, " +
            "d.dataset_name " +
            "FROM dataset_document_segment s " +
            "LEFT JOIN user cu ON s.creator_id = cu.user_id " +
            "LEFT JOIN user uu ON s.updator_id = uu.user_id " +
            "LEFT JOIN dataset_document dd ON s.document_id = dd.document_id " +
            "LEFT JOIN dataset d ON s.dataset_id = d.dataset_id " +
            "WHERE s.dataset_id = #{datasetId} " +
            "ORDER BY s.document_id, s.position ASC")
    List<DatasetDocumentSegmentDTO> getSegmentsByDatasetId(@Param("datasetId") Long datasetId);
}