package com.yxboot.ai.document.splitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yxboot.ai.document.Document;
import com.yxboot.ai.document.DocumentSegment;

class CharacterSplitterTest {

    @Test
    void split_longLineWithoutPunctuation_shouldTerminateWithOverlap() {
        CharacterSplitter splitter = new CharacterSplitter(300, 50);
        String longLine = "a".repeat(5000);
        List<DocumentSegment> segments = splitter.split(Document.of(longLine));

        int expectedMin = (int) Math.ceil(5000.0 / (300 - 50));
        assertTrue(segments.size() >= expectedMin);
        assertTrue(segments.size() <= expectedMin + 2,
                "expected bounded chunks, got " + segments.size());
        for (DocumentSegment segment : segments) {
            assertTrue(segment.getContent().length() <= 300);
        }
    }

    @Test
    void split_longLineWithoutPunctuation_lastChunkSmallerThanOverlap_shouldNotLoop() {
        CharacterSplitter splitter = new CharacterSplitter(300, 50);
        // 1000 字符会在 tail 产生 <= 50 的剩余段，旧逻辑会卡死
        String text = "x".repeat(1000);
        List<DocumentSegment> segments = splitter.split(Document.of(text));

        assertEquals(4, segments.size());
    }
}
