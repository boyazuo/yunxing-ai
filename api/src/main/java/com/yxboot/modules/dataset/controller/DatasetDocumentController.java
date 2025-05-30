package com.yxboot.modules.dataset.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.dataset.dto.DatasetDocumentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.dataset.enums.SegmentMethod;
import com.yxboot.modules.dataset.service.DatasetDocumentAsyncService;
import com.yxboot.modules.dataset.service.DatasetDocumentSegmentService;
import com.yxboot.modules.dataset.service.DatasetDocumentService;
import com.yxboot.modules.system.entity.SysFile;
import com.yxboot.modules.system.service.SysFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 知识库文档控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/dataset-documents")
@Tag(name = "知识库文档API", description = "知识库文档管理相关接口")
@RequiredArgsConstructor
public class DatasetDocumentController {

    private final DatasetDocumentService datasetDocumentService;
    private final DatasetDocumentAsyncService datasetDocumentAsyncService;
    private final DatasetDocumentSegmentService datasetDocumentSegmentService;
    private final SysFileService sysFileService;

    @GetMapping
    @Operation(summary = "获取文档列表", description = "根据知识库ID获取文档列表")
    public Result<List<DatasetDocumentDTO>> getDocumentsByDatasetId(
            @Parameter(description = "知识库ID") @RequestParam Long datasetId) {
        if (datasetId == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "知识库ID不能为空");
        }
        List<DatasetDocumentDTO> documents = datasetDocumentService.listDocumentsByDatasetId(datasetId);
        return Result.success("查询成功", documents);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取文档列表", description = "根据知识库ID分页获取文档列表")
    public Result<IPage<DatasetDocumentDTO>> getDocumentsPageByDatasetId(
            @Parameter(description = "知识库ID") @RequestParam Long datasetId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") long size) {
        if (datasetId == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "知识库ID不能为空");
        }
        IPage<DatasetDocumentDTO> documents = datasetDocumentService.getDocumentsByDatasetId(datasetId, current, size);
        return Result.success("查询成功", documents);
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "获取文档详情", description = "根据文档ID获取文档详情")
    public Result<DatasetDocument> getDocumentById(@PathVariable Long documentId) {
        DatasetDocument document = datasetDocumentService.getById(documentId);
        if (document == null) {
            return Result.error(ResultCode.NOT_FOUND, "文档不存在");
        }
        return Result.success("查询成功", document);
    }

    @PostMapping
    @Operation(summary = "创建文档", description = "创建新的文档")
    public Result<DatasetDocument> createDocument(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody DocumentRequest documentRequest) {
        // 参数验证
        if (documentRequest.getTenantId() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "租户ID不能为空");
        }
        if (documentRequest.getDatasetId() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "知识库ID不能为空");
        }
        if (documentRequest.getFileId() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "文件ID不能为空");
        }
        if (documentRequest.getFileName() == null || documentRequest.getFileName().trim().isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "文件名称不能为空");
        }
        if (documentRequest.getFileSize() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "文件大小不能为空");
        }
        if (documentRequest.getSegmentMethod() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "分段方式不能为空");
        }
        if (documentRequest.getMaxSegmentLength() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "分段最大长度不能为空");
        }
        if (documentRequest.getOverlapLength() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "重叠长度不能为空");
        }

        // 检查文件是否存在
        SysFile existingFile = sysFileService.getById(documentRequest.getFileId());
        if (existingFile == null) {
            return Result.error(ResultCode.NOT_FOUND, "文件不存在");
        }

        // 检查是否已存在相同hash的文档
        String fileHash = existingFile.getHash();
        if (fileHash != null && !fileHash.trim().isEmpty()) {
            DatasetDocument existingDocument = datasetDocumentService.checkDocumentExistsByHash(
                    documentRequest.getTenantId(),
                    documentRequest.getDatasetId(),
                    fileHash);
            if (existingDocument != null) {
                return Result.error(ResultCode.VALIDATE_FAILED,
                        "该文档已存在于知识库中，文档名称：" + existingDocument.getFileName());
            }
        }

        // 创建文档
        DatasetDocument document = datasetDocumentService.createDocument(
                documentRequest.getTenantId(),
                documentRequest.getDatasetId(),
                documentRequest.getFileId(),
                documentRequest.getFileName(),
                documentRequest.getFileSize(),
                fileHash,
                documentRequest.getSegmentMethod(),
                documentRequest.getMaxSegmentLength(),
                documentRequest.getOverlapLength());

        // 触发异步文档处理，传递用户ID
        datasetDocumentAsyncService.processDocumentAsync(document.getDocumentId(), securityUser.getUserId());

        return Result.success("文档创建成功，正在后台处理", document);
    }

    @PutMapping("/{documentId}/status")
    @Operation(summary = "更新文档状态", description = "更新文档状态")
    public Result<Void> updateDocumentStatus(@PathVariable Long documentId, @RequestBody StatusRequest statusRequest) {
        if (statusRequest.getStatus() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "状态不能为空");
        }

        boolean updated = datasetDocumentService.updateDocumentStatus(documentId, statusRequest.getStatus());
        if (!updated) {
            return Result.error(ResultCode.FAIL, "文档状态更新失败");
        }

        return Result.success("文档状态已更新");
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "删除文档", description = "删除指定文档")
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        // 验证文档是否存在
        DatasetDocument existingDocument = datasetDocumentService.getById(documentId);
        if (existingDocument == null) {
            return Result.error(ResultCode.NOT_FOUND, "文档不存在");
        }

        // 删除文档分段
        datasetDocumentSegmentService.deleteSegmentsByDocumentId(documentId);

        // 删除文档
        boolean removed = datasetDocumentService.removeById(documentId);
        if (!removed) {
            return Result.error(ResultCode.FAIL, "文档删除失败");
        }

        return Result.success("文档已删除");
    }

    /**
     * 文档请求参数
     */
    @Data
    public static class DocumentRequest {
        private Long tenantId;
        private Long datasetId;
        private Long fileId;
        private String fileName;
        private Integer fileSize;
        private SegmentMethod segmentMethod;
        private Integer maxSegmentLength;
        private Integer overlapLength;
    }

    /**
     * 状态请求参数
     */
    @Data
    public static class StatusRequest {
        private DocumentStatus status;
    }
}