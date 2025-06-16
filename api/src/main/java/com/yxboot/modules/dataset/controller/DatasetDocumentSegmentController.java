package com.yxboot.modules.dataset.controller;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.modules.dataset.application.DatasetDocumentApplicationService;
import com.yxboot.modules.dataset.application.DatasetDocumentSegmentApplicationService;
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 文档分段控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/document-segments")
@Tag(name = "文档分段API", description = "文档分段管理相关接口")
@RequiredArgsConstructor
public class DatasetDocumentSegmentController {

    private final DatasetDocumentSegmentApplicationService datasetDocumentSegmentApplicationService;
    private final DatasetDocumentApplicationService datasetDocumentApplicationService;

    @GetMapping("/page")
    @Operation(summary = "分页获取文档分段", description = "根据文档ID分页获取分段列表，支持搜索过滤")
    public Result<IPage<DatasetDocumentSegmentDTO>> getSegmentsPage(
            @Parameter(description = "文档ID") @RequestParam Long documentId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {

        if (documentId == null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "文档ID不能为空");
        }

        try {
            IPage<DatasetDocumentSegmentDTO> segments;

            if (keyword != null && !keyword.trim().isEmpty()) {
                // 带搜索的分页查询
                segments = datasetDocumentSegmentApplicationService.pageSegmentsWithSearch(current, size, documentId,
                        keyword.trim());
            } else {
                // 普通分页查询
                segments = datasetDocumentSegmentApplicationService.pageSegmentsByDocumentId(current, size, documentId);
            }

            return Result.success("查询成功", segments);
        } catch (Exception e) {
            return Result.error(ResultCode.FAIL, "查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{segmentId}")
    @Operation(summary = "获取分段详情", description = "根据分段ID获取分段详情")
    public Result<DatasetDocumentSegment> getSegmentById(@PathVariable Long segmentId) {
        DatasetDocumentSegment segment = datasetDocumentSegmentApplicationService.getSegmentById(segmentId);
        if (segment == null) {
            return Result.error(ResultCode.NOT_FOUND, "分段不存在");
        }
        return Result.success("查询成功", segment);
    }

    @PutMapping("/{segmentId}")
    @Operation(summary = "更新分段内容", description = "更新指定分段的标题和内容")
    public Result<Void> updateSegment(@PathVariable Long segmentId, @RequestBody UpdateSegmentRequest request) {

        // 参数验证
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "分段内容不能为空");
        }

        try {
            boolean success = datasetDocumentSegmentApplicationService.updateSegmentContent(segmentId,
                    request.getContent().trim(),
                    request.getTitle() != null ? request.getTitle().trim() : null);

            if (!success) {
                return Result.error(ResultCode.NOT_FOUND, "分段不存在或更新失败");
            }

            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(ResultCode.FAIL, "更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{segmentId}")
    @Operation(summary = "删除分段", description = "删除指定分段并同步删除向量库中对应的存储")
    public Result<Void> deleteSegment(@PathVariable Long segmentId) {
        try {
            // 1. 获取分段信息
            DatasetDocumentSegment segment = datasetDocumentSegmentApplicationService.getSegmentById(segmentId);
            if (segment == null) {
                return Result.error(ResultCode.NOT_FOUND, "分段不存在");
            }

            // 2. 删除分段并同步删除向量
            boolean success = datasetDocumentSegmentApplicationService.deleteSegmentCompletely(segmentId);
            if (!success) {
                return Result.error(ResultCode.FAIL, "删除失败");
            }

            // 3. 更新文档分段数量（通过文档应用服务）
            // 获取当前文档信息
            var document = datasetDocumentApplicationService.getDocumentById(segment.getDocumentId());
            if (document != null && document.getSegmentNum() > 0) {
                datasetDocumentApplicationService.updateDocumentSegmentNum(document.getDocumentId(),
                        document.getSegmentNum() - 1);
            }

            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(ResultCode.FAIL, "删除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除分段", description = "批量删除指定的分段并同步删除向量库中对应的存储")
    public Result<Void> batchDeleteSegments(@RequestBody BatchDeleteRequest request) {
        if (request.getSegmentIds() == null || request.getSegmentIds().isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "分段ID列表不能为空");
        }

        try {
            // 1. 获取要删除的分段信息（用于后续更新文档分段数）
            List<DatasetDocumentSegment> segmentsToDelete = request.getSegmentIds().stream()
                    .map(datasetDocumentSegmentApplicationService::getSegmentById).filter(segment -> segment != null)
                    .toList();

            if (segmentsToDelete.isEmpty()) {
                return Result.error(ResultCode.NOT_FOUND, "未找到任何有效的分段");
            }

            // 2. 批量删除分段并同步删除向量
            datasetDocumentSegmentApplicationService.batchDeleteSegmentsCompletely(request.getSegmentIds());

            // 3. 更新相关文档的分段数量
            Long documentId = segmentsToDelete.get(0).getDocumentId();
            var document = datasetDocumentApplicationService.getDocumentById(documentId);
            if (document != null && document.getSegmentNum() >= segmentsToDelete.size()) {
                datasetDocumentApplicationService.updateDocumentSegmentNum(documentId,
                        document.getSegmentNum() - segmentsToDelete.size());
            }

            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error(ResultCode.FAIL, "批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新分段请求参数
     */
    @Data
    public static class UpdateSegmentRequest {
        private String title;
        private String content;
    }

    /**
     * 批量删除请求参数
     */
    @Data
    public static class BatchDeleteRequest {
        private List<Long> segmentIds;
    }
}
