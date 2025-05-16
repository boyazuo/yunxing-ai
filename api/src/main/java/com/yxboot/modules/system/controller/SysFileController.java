package com.yxboot.modules.system.controller;

import com.yxboot.common.api.Result;
import com.yxboot.common.enums.StatusEnum;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.config.upload.FileEntity;
import com.yxboot.config.upload.uploader.Uploader;
import com.yxboot.modules.system.entity.SysFile;
import com.yxboot.modules.system.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 附件表 Api
 *
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/system/file")
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
@Tag(name = "【系统模块】附件表 Api")
public class SysFileController {
    private final SysFileService sysFileService;
    private final Uploader uploader;


    @GetMapping("/{attachmentId}")
    @Operation(summary = "附件表查询详情接口")
    public Result get(@PathVariable Long attachmentId) {
        SysFile coreAttachment = sysFileService.getById(attachmentId);
        return Result.success("查询成功！", coreAttachment );
    }

    @PostMapping
    @Operation(summary = "上传文件接口")
    public Result save(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal SecurityUser securityUser) {
        FileEntity fileEntity = uploader.upload(file);
        SysFile sysFile = new SysFile();
        sysFile.setOriginName(file.getOriginalFilename());
        sysFile.setFileName(fileEntity.getFileName());
        sysFile.setPath(fileEntity.getPath());
        sysFile.setUrl(fileEntity.getUrl());
        sysFile.setContentType(fileEntity.getContentType());
        sysFile.setSize(fileEntity.getSize());
        sysFile.setHash(fileEntity.getHash());
        sysFile.setCreateUserId(securityUser.getUserId());
        sysFile.setStatus(StatusEnum.INVALID.getValue()); // 默认无效
        sysFileService.saveOrUpdate(sysFile);
        fileEntity.setFileId(sysFile.getAttachmentId());
        return Result.success("保存成功！", fileEntity);
    }
}
