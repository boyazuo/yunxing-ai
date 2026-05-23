package com.yxboot.modules.account.service;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.account.entity.Invitation;
import com.yxboot.modules.account.mapper.InvitationMapper;

import lombok.RequiredArgsConstructor;

import static com.yxboot.modules.account.entity.table.InvitationTableDef.INVITATION;

/**
 * 邀请服务实现类
 */
@Service
@RequiredArgsConstructor
public class InvitationService extends ServiceImpl<InvitationMapper, Invitation> {

    public Invitation getByToken(String token) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(INVITATION.TOKEN.eq(token));
        return getOne(wrapper);
    }
}
