package in.handyman.raven.core.utils;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class DatabaseUtility {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtility.class);

    /**
     * Executes a SELECT query and returns a single result as Optional<String>.
     *
     * @param jdbiResourceName Name of the JDBI resource to use
     * @param sqlQuery         SQL query string with named parameters (e.g., :paramName)
     * @param parameters       Map of named parameters and their values
     * @return Optional containing the result, or empty if no match is found
     */
    public static Optional<String> fetchSingleResult(String jdbiResourceName, String sqlQuery, Map<String, Object> parameters) {
        try {
            Jdbi jdbiInstance = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
            return jdbiInstance.withHandle(handle -> {
                Query query = handle.createQuery(sqlQuery);
                parameters.forEach(query::bind);
                return query.mapTo(String.class).findOne();
            });
        } catch (Exception e) {
            logger.error("Error executing fetchSingleResult with query: {}, params: {}", sqlQuery, parameters, e);
            throw new HandymanException("Error in fetchSingleResult", e);
        }
    }

    /**
     * Fetches the BSH script source code from the configuration table based on class name and tenant ID.
     *
     * @param jdbiResourceName Name of the JDBI resource to use
     * @param className        The class name to search for
     * @param tenantId         Tenant ID for the lookup
     * @return Optional containing the BSH source code if found
     */
    public static Optional<String> fetchBshResultByClassName(String jdbiResourceName, String className, Long tenantId) {
        final String sqlQuery = "SELECT source_code FROM config.spw_bsh_config WHERE class_name = :className AND tenant_id = :tenantId";

        try {
            Jdbi jdbiInstance = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
            return jdbiInstance.withHandle(handle ->
                    handle.createQuery(sqlQuery)
                            .bind("className", className)
                            .bind("tenantId", tenantId)
                            .mapTo(String.class)
                            .findOne()
            );
        } catch (Exception e) {
            logger.error("Error executing fetchBshResultByClassName for class: {}, tenantId: {}", className, tenantId, e);
            throw new HandymanException("Error in fetchBshResultByClassName", e);
        }
    }
}
