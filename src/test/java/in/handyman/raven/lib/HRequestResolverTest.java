package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepoImpl;
import in.handyman.raven.lambda.process.HRequestResolver;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class HRequestResolverTest {

    private final HRequestResolver reqR2 = new HRequestResolver();

    @Test
    void testDbResolve() {
        Map<String, String> context = new HashMap<>();
        context.put("processContent", "DB resolution content");
        String result = reqR2.doResolve("TestLambda", "DB", context);
        assertEquals("DB resolution content", result);
        System.out.println(result);
    }

    @Test
    public void testFileResolve() {
        Map<String, String> context = new HashMap<>();
        context.put("filepath", "testfile.txt");
        context.put("basepath", "src/test/resources"); // Assuming test resources directory
        String result = reqR2.doResolve("TestLambda", "FILE", context);
        assertEquals("File content", result); // Assuming 'testfile.txt' contains "File content"
    }

    @Test
    public void testUnknownLambda() {
        Map<String, String> context = new HashMap<>();
        HandymanException exception = assertThrows(HandymanException.class, () -> {
            HRequestResolver.doResolve("UnknownLambda", "DB", context);
        });
        assertEquals("Lambda definition for UnknownLambda is unknown", exception.getMessage());
    }

    @Test
    public void testMissingFilePath() {
        Map<String, String> context = new HashMap<>();
        HandymanException exception = assertThrows(HandymanException.class, () -> {
            HRequestResolver.doResolve("TestLambda", "FILE", context);
        });
        assertEquals("File path configuration for process TestLambda is missing, check spw_process_config or spw_instance_config", exception.getMessage());
    }

    @Test
    public void testAbsentProcessDefinition() {
        Map<String, String> context = new HashMap<>();
        context.put("filepath", "nonexistentfile.txt");
        context.put("basepath", "src/test/resources"); // Assuming test resources directory
        HandymanException exception = assertThrows(HandymanException.class, () -> {
            HRequestResolver.doResolve("TestLambda", "FILE", context);
        });
        assertEquals("Process definition for src/test/resources/nonexistentfile.txt is absent", exception.getMessage());
    }
}

