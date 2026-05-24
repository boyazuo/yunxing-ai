package com.yxboot.modules.system.service;

import static com.yxboot.modules.system.entity.table.SysFileTableDef.SYS_FILE;
import org.springframework.stereotype.Service;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yxboot.modules.system.entity.SysFile;
import com.yxboot.modules.system.mapper.SysFileMapper;

/**
 * 附件服务实现类
 */
@Service
public class SysFileService extends ServiceImpl<SysFileMapper, SysFile> {

    public void updateStatus(Long fileId, Integer status) {
        SysFile entity = new SysFile();
        entity.setStatus(status);
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.where(SYS_FILE.FILE_ID.eq(fileId));
        update(entity, wrapper);
    }
}
