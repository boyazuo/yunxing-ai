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
import com.yxboot.common.exception.ApiException;
import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.dataset.application.DatasetDocumentApplicationService;
import com.yxboot.modules.dataset.application.DatasetDocumentProcessingApplicationService;
import com.yxboot.modules.dataset.dto.DatasetDocumentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocument;
import com.yxboot.modules.dataset.enums.DocumentStatus;
import com.yxboot.modules.dataset.enums.SegmentMethod;
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

    private final DatasetDocumentApplicationService datasetDocumentApplicationService;
    private final DatasetDocumentProcessingApplicationService datasetDocumentProcessingApplicationService;
    private final SysFileService sysFileService;

    @GetMapping
    @Operation(summary = "获取文档列表", description = "根据知识库ID获取文档列表")
    public Result<List<DatasetDocumentDTO>> getDocumentsByDatasetId(
            @Parameter(description = "知识库ID") @RequestParam Long datasetId) {
        if (datasetId == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "知识库ID不能为空");
        }
        List<DatasetDocumentDTO> documents = datasetDocumentApplicationService.listDocumentsByDatasetId(datasetId);
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
        IPage<DatasetDocumentDTO> documents = datasetDocumentApplicationService.getDocumentsByDatasetId(datasetId,
                current, size);
        return Result.success("查询成功", documents);
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "获取文档详情", description = "根据文档ID获取文档详情")
    public Result<DatasetDocument> getDocumentById(@PathVariable Long documentId) {
        DatasetDocument document = datasetDocumentApplicationService.getDocumentById(documentId);
        if (document == null) {
            return Result.error(ResultCode.NOT_FOUND, "文档不存在");
        }
        return Result.success("查询成功", document);
    }

    @PostMapping
    @Operation(summary = "创建文档", description = "创建新的文档")
    public Result<DatasetDocument> createDocument(@AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody DocumentRequest documentRequest) {
        // 检查文件是否存在
        SysFile existingFile = sysFileService.getById(documentRequest.getFileId());
        if (existingFile == null) {
            return Result.error(ResultCode.NOT_FOUND, "文件不存在");
        }

        try {
            // 创建文档（包含重复检查）
            DatasetDocument document = datasetDocumentApplicationService.createDocument(
                    documentRequest.getTenantId(),
                    documentRequest.getDatasetId(),
                    documentRequest.getFileId(),
                    documentRequest.getFileName(),
                    documentRequest.getFileSize(),
                    existingFile.getHash(),
                    documentRequest.getSegmentMethod(),
                    documentRequest.getMaxSegmentLength(),
                    documentRequest.getOverlapLength());

            // 触发异步文档处理，传递用户ID
            datasetDocumentProcessingApplicationService.processDocumentAsync(document.getDocumentId(), securityUser.getUserId());

            return Result.success("文档创建成功，正在后台处理", document);
        } catch (ApiException e) {
            return Result.error(e.getResultCode(), "文档创建失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(ResultCode.FAIL, "文档创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{documentId}/status")
    @Operation(summary = "更新文档状态", description = "更新文档状态")
    public Result<Void> updateDocumentStatus(@PathVariable Long documentId, @RequestBody StatusRequest statusRequest) {
        if (statusRequest.getStatus() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "状态不能为空");
        }

        boolean updated = datasetDocumentApplicationService.updateDocumentStatus(documentId, statusRequest.getStatus());
        if (!updated) {
            return Result.error(ResultCode.FAIL, "文档状态更新失败");
        }

        return Result.success("文档状态已更新");
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "删除文档", description = "删除指定文档及其相关数据")
    public Result<Void> deleteDocument(@PathVariable Long documentId,
            @AuthenticationPrincipal SecurityUser currentUser) {
        // 验证文档是否存在
        DatasetDocument existingDocument = datasetDocumentApplicationService.getDocumentById(documentId);
        if (existingDocument == null) {
            return Result.error(ResultCode.NOT_FOUND, "文档不存在");
        }

        // 删除文档及其相关数据（包括向量数据）
        boolean removed = datasetDocumentApplicationService.deleteDocumentCompletely(documentId);
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
