package in.handyman.raven.core.dataaccess;

public enum DataAccessKind {
    JDBC,
    ELASTICSEARCH,
    MONGODB,
    CASSANDRA,
    REDIS,
    DYNAMODB;

    public static DataAccessKind fromDriverClass(String driverClass) {
        if (driverClass == null || driverClass.isBlank()) {
            return JDBC;
        }
        String c = driverClass.trim().toLowerCase();

        if (c.contains(".jdbc.") || c.endsWith("driver")) {
            return JDBC;
        }
        if (c.contains("elasticsearch") || c.contains("opensearch")) {
            return ELASTICSEARCH;
        }
        if (c.contains("mongo")) {
            return MONGODB;
        }
        if (c.contains("cassandra") || c.contains("datastax") || c.contains("cqlsession")) {
            return CASSANDRA;
        }
        if (c.contains("jedis") || c.contains("lettuce") || c.contains(".redis.")) {
            return REDIS;
        }
        if (c.contains("dynamodb")) {
            return DYNAMODB;
        }
        return JDBC;
    }
}
