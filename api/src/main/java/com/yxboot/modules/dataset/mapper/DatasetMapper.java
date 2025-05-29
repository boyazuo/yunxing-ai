package com.yxboot.modules.dataset.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxboot.modules.dataset.dto.DatasetDTO;
import com.yxboot.modules.dataset.entity.Dataset;

/**
 * 知识库表 Mapper 接口
 * 
 * @author Boya
 */
public interface DatasetMapper extends BaseMapper<Dataset> {

    /**
     * 根据租户ID获取知识库列表，同时包含创建者和更新者信息
     * 
     * @param tenantId 租户ID
     * @return 知识库列表DTO
     */
    @Select("SELECT d.*, " +
            "cu.username as creator_username, " +
            "cu.avatar as creator_avatar, " +
            "uu.username as updator_username, " +
            "m.model_name as embedding_model_name " +
            "FROM dataset d " +
            "LEFT JOIN user cu ON d.creator_id = cu.user_id " +
            "LEFT JOIN user uu ON d.updator_id = uu.user_id " +
            "LEFT JOIN model m ON d.embedding_model_id = m.model_id " +
            "WHERE d.tenant_id = #{tenantId}")
    List<DatasetDTO> getDatasetsByTenantId(@Param("tenantId") String tenantId);
}