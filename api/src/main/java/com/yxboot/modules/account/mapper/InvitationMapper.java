package com.yxboot.modules.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxboot.modules.account.entity.Invitation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邀请记录Mapper
 */
@Mapper
public interface InvitationMapper extends BaseMapper<Invitation> {
}
