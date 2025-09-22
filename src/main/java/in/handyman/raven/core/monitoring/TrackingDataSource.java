package in.handyman.raven.core.monitoring;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrackingDataSource implements DataSource {

    private final DataSource delegate;
    private final Map<Connection, String> connectionTracker = new ConcurrentHashMap<>();

    public TrackingDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = delegate.getConnection();
        connectionTracker.put(conn, Thread.currentThread().getName());
        return new ConnectionWrapper(conn);
    }

    // Implement other DataSource methods by delegating

    private class ConnectionWrapper implements Connection {
        private final Connection delegateConnection;

        ConnectionWrapper(Connection delegateConnection) {
            this.delegateConnection = delegateConnection;
        }

        @Override
        public void close() throws SQLException {
            connectionTracker.remove(delegateConnection);
            delegateConnection.close();
        }

        // Delegate all other methods...
    }

    public Map<Connection, String> getActiveConnections() {
        return Collections.unmodifiableMap(connectionTracker);
    }
}

