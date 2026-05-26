package in.handyman.raven.lambda.access;

import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.core.dataaccess.DataAccessRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class ResourceAccess {

    public static HikariDataSource rdbmsConn(final String resourceName) {
        return DataAccessRegistry.get(resourceName).asDataSource();
    }

    public static Jdbi rdbmsJDBIConn(final String resourceName) {
        return DataAccessRegistry.get(resourceName).asJdbi();
    }

}
