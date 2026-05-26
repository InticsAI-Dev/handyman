package in.handyman.raven.lambda.access;

import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.core.azure.adapters.HikariJdbiProvider;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class ResourceAccess {

    public static HikariDataSource rdbmsConn(final String resourceName) {
        return HikariJdbiProvider.getDataSource(resourceName);
    }

    public static Jdbi rdbmsJDBIConn(final String resourceName) {
        return HikariJdbiProvider.getJdbi(resourceName);
    }

}
