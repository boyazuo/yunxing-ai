package com.yxboot.modules.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.account.entity.Invitation;
import com.yxboot.modules.account.mapper.InvitationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 邀请记录服务
 */
@Service
@Slf4j
public class InvitationService extends ServiceImpl<InvitationMapper, Invitation>
        implements IService<Invitation> {
}
