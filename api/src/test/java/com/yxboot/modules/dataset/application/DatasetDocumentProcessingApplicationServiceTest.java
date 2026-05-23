package com.yxboot.modules.dataset.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.yxboot.ai.document.splitter.SplitMode;
import com.yxboot.ai.service.AiDocumentProcessingService;
import com.yxboot.ai.service.AiVectorStoreService;
import com.yxboot.modules.dataset.enums.SegmentMethod;
import com.yxboot.modules.dataset.service.DatasetDocumentSegmentService;
import com.yxboot.modules.dataset.service.DatasetDocumentService;
import com.yxboot.modules.dataset.service.DatasetService;
import com.yxboot.modules.system.service.SysFileService;

/**
 * 文档处理应用服务测试类
 */
class DatasetDocumentProcessingApplicationServiceTest {

    @Mock
    private DatasetDocumentService datasetDocumentService;

    @Mock
    private DatasetDocumentSegmentService segmentService;

    @Mock
    private SysFileService sysFileService;

    @Mock
    private AiDocumentProcessingService documentProcessingService;

    @Mock
    private AiVectorStoreService vectorStoreService;

    @Mock
    private DatasetService datasetService;

    private DatasetDocumentProcessingApplicationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationService = new DatasetDocumentProcessingApplicationService(
                datasetDocumentService,
                segmentService,
                sysFileService,
                documentProcessingService,
                vectorStoreService,
                datasetService);
    }

    @Test
    void testConvertSegmentMethodToSplitMode_Paragraph() throws Exception {
        Method method = DatasetDocumentProcessingApplicationService.class
                .getDeclaredMethod("convertSegmentMethodToSplitMode", SegmentMethod.class);
        method.setAccessible(true);
        SplitMode result = (SplitMode) method.invoke(applicationService, SegmentMethod.PARAGRAPH);
        assertEquals(SplitMode.CHARACTER_SPLITTER, result);
    }

    @Test
    void testConvertSegmentMethodToSplitMode_Chapter() throws Exception {
        Method method = DatasetDocumentProcessingApplicationService.class
                .getDeclaredMethod("convertSegmentMethodToSplitMode", SegmentMethod.class);
        method.setAccessible(true);
        SplitMode result = (SplitMode) method.invoke(applicationService, SegmentMethod.CHAPTER);
        assertEquals(SplitMode.CHAPTER_SPLITTER, result);
    }

    @Test
    void testConvertSegmentMethodToSplitMode_Null() throws Exception {
        Method method = DatasetDocumentProcessingApplicationService.class
                .getDeclaredMethod("convertSegmentMethodToSplitMode", SegmentMethod.class);
        method.setAccessible(true);
        SplitMode result = (SplitMode) method.invoke(applicationService, (SegmentMethod) null);
        assertEquals(SplitMode.CHARACTER_SPLITTER, result);
    }
}
