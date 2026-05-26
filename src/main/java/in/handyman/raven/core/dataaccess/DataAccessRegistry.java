package in.handyman.raven.core.dataaccess;

import in.handyman.raven.core.dataaccess.elasticsearch.ElasticsearchDataAccessProvider;
import in.handyman.raven.core.dataaccess.jdbc.JdbcDataAccessProvider;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ConfigAccess;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public final class DataAccessRegistry {

    private static final Logger log = LoggerFactory.getLogger(DataAccessRegistry.class);

    private static final ConcurrentHashMap<String, DataAccessProvider> PROVIDERS = new ConcurrentHashMap<>();

    private DataAccessRegistry() {
    }

    public static DataAccessProvider system() {
        return JdbcDataAccessProvider.system();
    }

    public static DataAccessProvider get(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            return system();
        }
        return PROVIDERS.computeIfAbsent(resourceName, DataAccessRegistry::build);
    }

    private static DataAccessProvider build(String resourceName) {
        SpwResourceConfig resource;
        try {
            resource = ConfigAccess.getResourceConfig(resourceName);
        } catch (Exception e) {
            throw new HandymanException("Failed to look up resource '" + resourceName + "'", e);
        }
        if (resource == null) {
            throw new HandymanException("Resource '" + resourceName + "' not found in spw_resource_config");
        }
        DataAccessKind kind = DataAccessKind.fromDriverClass(resource.getDriverClass());
        log.info("Building {} provider for resource '{}' url={}", kind, resourceName, resource.getResourceUrl());
        switch (kind) {
            case JDBC:
                return new JdbcDataAccessProvider(resourceName);
            case ELASTICSEARCH:
                return new ElasticsearchDataAccessProvider(resourceName, resource);
            default:
                throw new HandymanException("Kind " + kind + " not yet implemented for resource '" + resourceName + "'");
        }
    }

    public static void shutdown() {
        PROVIDERS.values().forEach(p -> {
            try {
                p.close();
            } catch (Exception e) {
                log.warn("Error closing provider '{}'", p.name(), e);
            }
        });
        PROVIDERS.clear();
    }
}
