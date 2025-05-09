package com.yxboot.modules.ai.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxboot.modules.ai.dto.ModelDTO;
import com.yxboot.modules.ai.entity.Model;

/**
 * 模型 Mapper 接口
 * 
 * @author Boya
 */
public interface ModelMapper extends BaseMapper<Model> {

        /**
         * 获取提供商下的所有模型列表
         * 
         * @param providerId 提供商ID
         * @return 模型列表
         */
        @Select("SELECT m.*, " +
                        "p.provider_name, " +
                        "p.logo as provider_logo " +
                        "FROM model m " +
                        "LEFT JOIN provider p ON m.provider_id = p.provider_id " +
                        "WHERE m.provider_id = #{providerId}")
        List<ModelDTO> getModelsByProviderId(@Param("providerId") Long providerId);

}