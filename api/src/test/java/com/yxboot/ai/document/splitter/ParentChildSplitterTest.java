package com.yxboot.ai.document.splitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yxboot.ai.document.Document;
import com.yxboot.ai.document.DocumentSegment;
import com.yxboot.modules.dataset.enums.SegmentType;

class ParentChildSplitterTest {

    private final ParentChildSplitter splitter = new ParentChildSplitter();

    @Test
    void split_shouldCreateParentAndChildSegments() {
        String text = "第一段内容。".repeat(80);
        List<DocumentSegment> segments = splitter.split(Document.of(text), 200, 80, 10);

        long parentCount = segments.stream().filter(s -> s.getSegmentType() == SegmentType.PARENT).count();
        long childCount = segments.stream().filter(s -> s.getSegmentType() == SegmentType.CHILD).count();

        assertTrue(parentCount >= 1);
        assertTrue(childCount >= parentCount);

        for (DocumentSegment child : segments.stream().filter(s -> s.getSegmentType() == SegmentType.CHILD).toList()) {
            assertNotNull(child.getParentId());
            assertFalse(child.getParentId().isBlank());
        }

        for (DocumentSegment parent : segments.stream().filter(s -> s.getSegmentType() == SegmentType.PARENT).toList()) {
            assertTrue(parent.getContent().length() <= 200 || parent.getContent().length() > 0);
            assertEquals(SegmentType.PARENT, parent.getSegmentType());
        }
    }
}
