package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.deepSiftSearch.DeepSiftSearchConsumerProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeepSiftSearchConsumerProcessTest {

    private DeepSiftSearchConsumerProcess processor;

    @BeforeEach
    void setup() {

        Logger log = LoggerFactory.getLogger(getClass());

        processor = DeepSiftSearchConsumerProcess.builder()
                .log(log)
                .marker(MarkerFactory.getMarker("TEST"))
                .action(new ActionExecutionAudit())
                .pageContentMinLength(5)
                .build();
    }

    @Test
    void testFindKeywordBboxExactMatch() throws Exception {

        String bboxJson = """
                [
                  {
                    "text":"prior",
                    "bbox":[10,20,50,60]
                  },
                  {
                    "text":"auth",
                    "bbox":[55,20,100,60]
                  }
                ]
                """;

        Method parseMethod = DeepSiftSearchConsumerProcess.class
                .getDeclaredMethod("parseBboxNodes", String.class);

        parseMethod.setAccessible(true);

        List<Map<String, Object>> bboxNodes =
                (List<Map<String, Object>>) parseMethod.invoke(processor, bboxJson);

        assertNotNull(bboxNodes);
        assertEquals(2, bboxNodes.size());

        Method indexMethod = DeepSiftSearchConsumerProcess.class
                .getDeclaredMethod("buildBboxWordIndex", List.class);

        indexMethod.setAccessible(true);

        Map<String, List<Integer>> bboxIndex =
                (Map<String, List<Integer>>) indexMethod.invoke(processor, bboxNodes);

        assertNotNull(bboxIndex);
        assertTrue(bboxIndex.containsKey("prior"));
        assertTrue(bboxIndex.containsKey("auth"));

        Method findMethod = DeepSiftSearchConsumerProcess.class
                .getDeclaredMethod(
                        "findKeywordBbox",
                        String.class,
                        String.class,
                        List.class,
                        Map.class
                );

        findMethod.setAccessible(true);

        Map<String, Object> bbox =
                (Map<String, Object>) findMethod.invoke(
                        processor,
                        "prior auth",
                        "exact",
                        bboxNodes,
                        bboxIndex
                );

        assertNotNull(bbox);

        assertEquals(10, bbox.get("topLeftX"));
        assertEquals(20, bbox.get("topLeftY"));
        assertEquals(100, bbox.get("bottomRightX"));
        assertEquals(60, bbox.get("bottomRightY"));
    }
}