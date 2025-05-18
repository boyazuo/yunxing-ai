package com.yxboot.modules.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxboot.modules.account.entity.InvitationRecord;
import com.yxboot.modules.account.mapper.InvitationRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 邀请记录服务
 */
@Service
@Slf4j
public class InvitationRecordService extends ServiceImpl<InvitationRecordMapper, InvitationRecord>
        implements IService<InvitationRecord> {
}
