package in.handyman.raven.core.dataaccess;

import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

public interface DataAccessProvider {

    String name();

    DataAccessKind kind();

    default Jdbi asJdbi() {
        throw new UnsupportedOperationException("Resource '" + name() + "' is " + kind() + ", not JDBC. asJdbi() is not supported.");
    }

    default HikariDataSource asDataSource() {
        throw new UnsupportedOperationException("Resource '" + name() + "' is " + kind() + ", not JDBC. asDataSource() is not supported.");
    }

    default Object asElastic() {
        throw new UnsupportedOperationException("Resource '" + name() + "' is " + kind() + ", not Elasticsearch. asElastic() is not supported.");
    }

    default void close() {}
}
