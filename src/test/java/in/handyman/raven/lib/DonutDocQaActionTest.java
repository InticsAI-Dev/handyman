package in.handyman.raven.lib;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.logging.Logger;

@Slf4j
public class DonutDocQaActionTest {

    static final Logger logger = (Logger) LoggerFactory.getLogger(DonutDocQaActionTest.class);

    @Test
    public void loggerTest() {

        logger.info("This is info logger ");
    }
}