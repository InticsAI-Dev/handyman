package in.handyman.raven.access;

import in.handyman.raven.lambda.access.ConfigAccess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class ConfigAccessDTest {

    @Test
    void getResourceConfig() {
        var commonConfig = ConfigAccess.getResourceConfig("intics_zio_db_conn");
        assert commonConfig != null;
        log.info(commonConfig.toString());
        System.out.println(commonConfig);
    }


}
