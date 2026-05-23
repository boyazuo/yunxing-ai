package com.yxboot.ai.vector;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量检索结果（业务层 DTO，与历史 QueryResult 字段兼容）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQueryResult {

    private String id;
    private String text;
    private float score;
    private Map<String, Object> metadata;

    /** 命中的子块 ID（parent_child 策略下填充） */
    private Long hitChildSegmentId;

    /** 命中的子块内容（用于调试） */
    private String hitChildContent;
}
