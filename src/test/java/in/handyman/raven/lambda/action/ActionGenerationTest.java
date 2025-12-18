package in.handyman.raven.lambda.action;

import in.handyman.raven.compiler.RavenParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class ActionGenerationTest {

    @Test
    void generate() {
        final List<Class<?>> classes = Arrays.asList(RavenParser.class.getDeclaredClasses());
        final ActionGeneration actionGeneration = new ActionGeneration();
        actionGeneration.generate(classes, "in.handyman.raven.lib.model", "in.handyman.raven.lib");
    }
}
