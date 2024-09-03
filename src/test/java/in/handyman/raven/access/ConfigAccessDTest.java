package in.handyman.raven.access;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ConfigAccess;
import in.handyman.raven.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
class ConfigAccessDTest {

    @Test
    void getResourceConfig() {
        final Map<String, String> commonConfig = ConfigAccess.getCommonConfig();
        assert commonConfig != null;
        log.info(commonConfig.toString());
    }



}
