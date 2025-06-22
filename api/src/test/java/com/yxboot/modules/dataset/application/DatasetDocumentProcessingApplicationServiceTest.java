package com.yxboot.modules.dataset.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.yxboot.llm.client.document.DocumentProcessorClient;
import com.yxboot.llm.client.vector.VectorStoreClient;
import com.yxboot.llm.document.splitter.SplitMode;
import com.yxboot.modules.ai.service.ModelService;
import com.yxboot.modules.ai.service.ProviderService;
import com.yxboot.modules.dataset.enums.SegmentMethod;
import com.yxboot.modules.dataset.service.DatasetDocumentSegmentService;
import com.yxboot.modules.dataset.service.DatasetDocumentService;
import com.yxboot.modules.dataset.service.DatasetService;
import com.yxboot.modules.system.service.SysFileService;

/**
 * 文档处理应用服务测试类
 *
 * @author Boya
 */
class DatasetDocumentProcessingApplicationServiceTest {

    @Mock
    private DatasetDocumentService datasetDocumentService;

    @Mock
    private DatasetDocumentSegmentService segmentService;

    @Mock
    private SysFileService sysFileService;

    @Mock
    private DocumentProcessorClient documentProcessorClient;

    @Mock
    private VectorStoreClient vectorService;

    @Mock
    private ProviderService providerService;

    @Mock
    private DatasetService datasetService;

    @Mock
    private ModelService modelService;

    private DatasetDocumentProcessingApplicationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationService = new DatasetDocumentProcessingApplicationService(
                datasetDocumentService,
                segmentService,
                sysFileService,
                documentProcessorClient,
                vectorService,
                providerService,
                datasetService,
                modelService);
    }

    @Test
    void testConvertSegmentMethodToSplitMode_Paragraph() throws Exception {
        // 使用反射调用私有方法
        Method method = DatasetDocumentProcessingApplicationService.class
                .getDeclaredMethod("convertSegmentMethodToSplitMode", SegmentMethod.class);
        method.setAccessible(true);

        SplitMode result = (SplitMode) method.invoke(applicationService, SegmentMethod.PARAGRAPH);
        assertEquals(SplitMode.CHARACTER_SPLITTER, result);
    }

    @Test
    void testConvertSegmentMethodToSplitMode_Chapter() throws Exception {
        // 使用反射调用私有方法
        Method method = DatasetDocumentProcessingApplicationService.class
                .getDeclaredMethod("convertSegmentMethodToSplitMode", SegmentMethod.class);
        method.setAccessible(true);

        SplitMode result = (SplitMode) method.invoke(applicationService, SegmentMethod.CHAPTER);
        assertEquals(SplitMode.CHAPTER_SPLITTER, result);
    }

    @Test
    void testConvertSegmentMethodToSplitMode_Null() throws Exception {
        // 使用反射调用私有方法
        Method method = DatasetDocumentProcessingApplicationService.class
                .getDeclaredMethod("convertSegmentMethodToSplitMode", SegmentMethod.class);
        method.setAccessible(true);

        SplitMode result = (SplitMode) method.invoke(applicationService, (SegmentMethod) null);
        assertEquals(SplitMode.CHARACTER_SPLITTER, result); // 默认值应该是字符长度分割
    }
}
