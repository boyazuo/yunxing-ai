package com.yxboot.modules.dataset.controller;

import java.util.List;

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
import com.yxboot.modules.dataset.dto.DatasetDocumentSegmentDTO;
import com.yxboot.modules.dataset.entity.DatasetDocumentSegment;
import com.yxboot.modules.dataset.service.DatasetDocumentSegmentService;

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
@RequestMapping("/v1/api/documents/{documentId}/segments")
@Tag(name = "文档分段API", description = "文档分段管理相关接口")
@RequiredArgsConstructor
public class DatasetDocumentSegmentController {

    private final DatasetDocumentSegmentService segmentService;

    @GetMapping
    @Operation(summary = "获取文档分段列表", description = "获取指定文档的所有分段")
    public Result<List<DatasetDocumentSegmentDTO>> getSegments(
            @PathVariable @Parameter(description = "文档ID") Long documentId) {
        List<DatasetDocumentSegmentDTO> segments = segmentService.getSegmentsByDocumentId(documentId);
        return Result.success("查询成功", segments);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取文档分段", description = "分页获取指定文档的分段")
    public Result<IPage<DatasetDocumentSegmentDTO>> pageSegments(
            @PathVariable @Parameter(description = "文档ID") Long documentId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size) {
        IPage<DatasetDocumentSegmentDTO> segmentPage = segmentService.pageSegmentsByDocumentId(page, size, documentId);
        return Result.success("查询成功", segmentPage);
    }

    @GetMapping("/{segmentId}")
    @Operation(summary = "获取分段详情", description = "根据分段ID获取分段详情")
    public Result<DatasetDocumentSegment> getSegmentById(
            @PathVariable @Parameter(description = "文档ID") Long documentId,
            @PathVariable @Parameter(description = "分段ID") Long segmentId) {
        DatasetDocumentSegment segment = segmentService.getById(segmentId);
        if (segment == null) {
            return Result.error(ResultCode.NOT_FOUND, "分段不存在");
        }
        if (!segment.getDocumentId().equals(documentId)) {
            return Result.error(ResultCode.VALIDATE_FAILED, "分段不属于指定文档");
        }
        return Result.success("查询成功", segment);
    }

    @PostMapping
    @Operation(summary = "批量创建文档分段", description = "批量创建文档分段")
    public Result<Integer> createSegments(
            @PathVariable @Parameter(description = "文档ID") Long documentId,
            @RequestBody BatchCreateRequest request) {
        if (request.getSegments() == null || request.getSegments().isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED, "分段内容不能为空");
        }

        int count = segmentService.batchCreateSegments(documentId, request.getSegments(), request.getTitles());
        return Result.success("创建成功", count);
    }

    @PutMapping("/{segmentId}")
    @Operation(summary = "更新分段内容", description = "更新指定分段的内容")
    public Result<Void> updateSegment(
            @PathVariable @Parameter(description = "文档ID") Long documentId,
            @PathVariable @Parameter(description = "分段ID") Long segmentId,
            @RequestBody UpdateSegmentRequest request) {
        // 验证分段是否存在且属于指定文档
        DatasetDocumentSegment segment = segmentService.getById(segmentId);
        if (segment == null) {
            return Result.error(ResultCode.NOT_FOUND, "分段不存在");
        }
        if (!segment.getDocumentId().equals(documentId)) {
            return Result.error(ResultCode.VALIDATE_FAILED, "分段不属于指定文档");
        }

        // 更新分段内容
        boolean updated = segmentService.updateSegmentContent(segmentId, request.getContent(), request.getTitle());
        if (!updated) {
            return Result.error(ResultCode.FAIL, "更新失败");
        }

        return Result.success("更新成功");
    }

    @DeleteMapping
    @Operation(summary = "删除文档所有分段", description = "删除指定文档的所有分段")
    public Result<Void> deleteSegments(
            @PathVariable @Parameter(description = "文档ID") Long documentId) {
        boolean deleted = segmentService.deleteSegmentsByDocumentId(documentId);
        if (!deleted) {
            return Result.error(ResultCode.FAIL, "删除失败");
        }

        return Result.success("删除成功");
    }

    @DeleteMapping("/{segmentId}")
    @Operation(summary = "删除单个分段", description = "删除指定分段")
    public Result<Void> deleteSegment(
            @PathVariable @Parameter(description = "文档ID") Long documentId,
            @PathVariable @Parameter(description = "分段ID") Long segmentId) {
        // 验证分段是否存在且属于指定文档
        DatasetDocumentSegment segment = segmentService.getById(segmentId);
        if (segment == null) {
            return Result.error(ResultCode.NOT_FOUND, "分段不存在");
        }
        if (!segment.getDocumentId().equals(documentId)) {
            return Result.error(ResultCode.VALIDATE_FAILED, "分段不属于指定文档");
        }

        // 删除分段
        boolean deleted = segmentService.removeById(segmentId);
        if (!deleted) {
            return Result.error(ResultCode.FAIL, "删除失败");
        }

        return Result.success("删除成功");
    }

    @Data
    public static class BatchCreateRequest {
        private List<String> segments;
        private List<String> titles;
    }

    @Data
    public static class UpdateSegmentRequest {
        private String content;
        private String title;
    }
}