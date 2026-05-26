package in.handyman.raven.core.dataaccess.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.core.azure.adapters.HikariJdbiProvider;
import in.handyman.raven.core.dataaccess.DataAccessKind;
import in.handyman.raven.core.dataaccess.DataAccessProvider;
import org.jdbi.v3.core.Jdbi;

public class JdbcDataAccessProvider implements DataAccessProvider {

    private static final String SYSTEM = "__system__";

    private final String name;

    public JdbcDataAccessProvider(String name) {
        this.name = name;
    }

    public static JdbcDataAccessProvider system() {
        return new JdbcDataAccessProvider(SYSTEM);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DataAccessKind kind() {
        return DataAccessKind.JDBC;
    }

    @Override
    public Jdbi asJdbi() {
        return SYSTEM.equals(name) ? HikariJdbiProvider.getJdbi() : HikariJdbiProvider.getJdbi(name);
    }

    @Override
    public HikariDataSource asDataSource() {
        return SYSTEM.equals(name) ? HikariJdbiProvider.getDataSource() : HikariJdbiProvider.getDataSource(name);
    }
}
