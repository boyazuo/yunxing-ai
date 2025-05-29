package com.yxboot.modules.dataset.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yxboot.modules.dataset.dto.DatasetDocumentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;

/**
 * 知识库文档表 Mapper 接口
 * 
 * @author Boya
 */
public interface DatasetDocumentMapper extends BaseMapper<DatasetDocument> {

    /**
     * 分页获取知识库文档列表，包含创建者和知识库信息
     * 
     * @param page      分页参数
     * @param datasetId 知识库ID
     * @return 文档列表分页数据
     */
    @Select("SELECT dd.*, " +
            "ds.dataset_name, " +
            "cu.username as creator_username, " +
            "cu.avatar as creator_avatar, " +
            "uu.username as updator_username " +
            "FROM dataset_document dd " +
            "LEFT JOIN dataset ds ON dd.dataset_id = ds.dataset_id " +
            "LEFT JOIN user cu ON dd.creator_id = cu.user_id " +
            "LEFT JOIN user uu ON dd.updator_id = uu.user_id " +
            "WHERE dd.dataset_id = #{datasetId} " +
            "ORDER BY dd.create_time DESC")
    IPage<DatasetDocumentDTO> getDocumentsByDatasetId(Page<DatasetDocumentDTO> page,
            @Param("datasetId") Long datasetId);

    /**
     * 根据知识库ID获取文档列表，包含创建者和知识库信息
     * 
     * @param datasetId 知识库ID
     * @return 文档列表
     */
    @Select("SELECT dd.*, " +
            "ds.dataset_name, " +
            "cu.username as creator_username, " +
            "cu.avatar as creator_avatar, " +
            "uu.username as updator_username " +
            "FROM dataset_document dd " +
            "LEFT JOIN dataset ds ON dd.dataset_id = ds.dataset_id " +
            "LEFT JOIN user cu ON dd.creator_id = cu.user_id " +
            "LEFT JOIN user uu ON dd.updator_id = uu.user_id " +
            "WHERE dd.dataset_id = #{datasetId} " +
            "ORDER BY dd.create_time DESC")
    List<DatasetDocumentDTO> listDocumentsByDatasetId(@Param("datasetId") Long datasetId);

    /**
     * 根据租户ID获取文档列表，包含创建者和知识库信息
     * 
     * @param tenantId 租户ID
     * @return 文档列表
     */
    @Select("SELECT dd.*, " +
            "ds.dataset_name, " +
            "cu.username as creator_username, " +
            "cu.avatar as creator_avatar, " +
            "uu.username as updator_username " +
            "FROM dataset_document dd " +
            "LEFT JOIN dataset ds ON dd.dataset_id = ds.dataset_id " +
            "LEFT JOIN user cu ON dd.creator_id = cu.user_id " +
            "LEFT JOIN user uu ON dd.updator_id = uu.user_id " +
            "WHERE dd.tenant_id = #{tenantId} " +
            "ORDER BY dd.create_time DESC")
    List<DatasetDocumentDTO> listDocumentsByTenantId(@Param("tenantId") Long tenantId);
}