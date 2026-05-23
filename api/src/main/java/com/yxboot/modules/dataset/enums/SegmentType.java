package com.yxboot.modules.dataset.enums;

/**
 * 文档分段类型
 */
public final class SegmentType {

    /** 普通分段（paragraph / chapter 策略，历史数据） */
    public static final int NORMAL = 0;

    /** 父块（parent_child 策略，不进向量库） */
    public static final int PARENT = 1;

    /** 子块（parent_child 策略，进向量库） */
    public static final int CHILD = 2;

    private SegmentType() {
    }
}
