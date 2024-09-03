package in.handyman.raven.lib.prompt.generation;

import in.handyman.raven.util.CommonQueryUtil;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;


public class PromptEngineProcess {

    private Jdbi jdbi;
    private final Logger log;

    public PromptEngineProcess(Jdbi jdbi, Logger log) {
        this.jdbi = jdbi;
        this.log = log;
    }


    public String generatePrompt(String basePrompt, Map<String, String> placeholders) {
        String generatedPrompt = basePrompt;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue();
            generatedPrompt = generatedPrompt.replace(placeholder, value);
        }

        return generatedPrompt;
    }


    public Map<String, String> fetchDynamicValues(Map<String, String> placeholdersWithQueries) {
        Map<String, String> dynamicValues = new HashMap<>();

        for (Map.Entry<String, String> entry : placeholdersWithQueries.entrySet()) {
            String placeholder = entry.getKey();
            String query = entry.getValue();

            // Validate and sanitize the query before execution
            if (isValidQuery(query)) {
                String value = jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).findOnly());
                dynamicValues.put(placeholder, value);
            } else {
                log.info("Invalid query for placeholder: " + placeholder);
                // Optionally, set a default value or handle invalid queries
            }
        }

        return dynamicValues;
    }


    public boolean isValidQuery(String query) {
        // Example validation: Ensure the query doesn't contain harmful SQL keywords or characters
        String lowerCaseQuery = query.toLowerCase();
        if (lowerCaseQuery.contains("drop ") || lowerCaseQuery.contains("delete ") ||
                lowerCaseQuery.contains("--") || lowerCaseQuery.contains(";")) {
            return false;  // Reject queries containing dangerous operations
        }

        // Further validation can be done here (e.g., regex patterns, whitelist/blacklist of SQL keywords)

        return true;
    }

    public List<PromptGenerationInputTable> promptGenerationInputTables(String inputSelectQuery){
        List<PromptGenerationInputTable> tableInfos = new ArrayList<>();
        jdbi.useTransaction(handle -> {
            final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(inputSelectQuery);
            formattedQuery.forEach(sqlToExecute -> {
                Query query = handle.createQuery(sqlToExecute);
                ResultIterable<PromptGenerationInputTable> resultIterable = query.mapToBean(PromptGenerationInputTable.class);
                List<PromptGenerationInputTable> detailList = resultIterable.stream().collect(Collectors.toList());
                tableInfos.addAll(detailList);
            });
        });
        return tableInfos;
    }
}
