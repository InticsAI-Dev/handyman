package in.handyman.raven.core.utils;


import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;

import java.util.Map;
import java.util.Optional;

public class DatabaseUtility {

    /**
     * Executes a SELECT query and returns a single result.
     * @param jdbi Jdbi connection instance
     * @param sqlQuery SQL query string with named parameters (e.g., :paramName)
     * @param parameters Map of named parameters and their values
     * @return Optional containing the result, or empty if no match is found
     */
    public static Optional<String> fetchSingleResult(Jdbi jdbi, String sqlQuery, Map<String, Object> parameters) {
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(sqlQuery);

            // Bind parameters dynamically
            parameters.forEach(query::bind);

            return query.mapTo(String.class).findOne();
        });
    }

    public static Optional<String> fetchBshResultByClassName(Jdbi jdbi,String className,Long tenantId) {
        String sqlQuery = "SELECT source_code\n" +
                "FROM config.spw_bsh_config\n" +
                "where class_name = :className and tenant_id = :tenantId;";
        return jdbi.withHandle(handle -> {

            // Bind parameters dynamically
            Query dbQuery = handle.createQuery(sqlQuery)
                    .bind("className", className)
                    .bind("tenantId", tenantId);

            return dbQuery.mapTo(String.class).findOne();
        });
    }



}
