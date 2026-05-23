package com.yxboot.modules.system.service;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.system.entity.SysFile;
import com.yxboot.modules.system.mapper.SysFileMapper;

import static com.yxboot.modules.system.entity.table.SysFileTableDef.SYS_FILE;

/**
 * 附件服务实现类
 */
@Service
public class SysFileService extends ServiceImpl<SysFileMapper, SysFile> {

    public void updateStatus(Long attachmentId, Integer status) {
        SysFile entity = new SysFile();
        entity.setStatus(status);
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(SYS_FILE.ATTACHMENT_ID.eq(attachmentId));
        update(entity, wrapper);
    }
}
