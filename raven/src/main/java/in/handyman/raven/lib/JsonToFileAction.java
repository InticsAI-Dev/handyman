package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.JsonToFile;
import in.handyman.raven.util.CommonQueryUtil;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "JsonToFile"
)
public class JsonToFileAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final JsonToFile jsonToFile;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Marker aMarker;

    public JsonToFileAction(final ActionExecutionAudit action, final Logger log,
                            final Object jsonToFile) {
        this.jsonToFile = (JsonToFile) jsonToFile;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" JsonToFile:" + this.jsonToFile.getName());
    }

    @Override
    public void execute() throws Exception {
        log.info(aMarker, "<-------Json toFile Action for {} has been started------->" + jsonToFile.getName());
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jsonToFile.getResourceConn());
        final List<Map<String, Object>> results = new ArrayList<>();
        jdbi.useTransaction(handle -> {
            final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(jsonToFile.getJsonSql());
            formattedQuery.forEach(sqlToExecute -> {
                results.addAll(handle.createQuery(sqlToExecute).mapToMap().stream().collect(Collectors.toList()));
            });
        });
        final int size = results.size();
        if (size > 0) {
            mapper.writeValue(new File(jsonToFile.getFilePath()), size > 1 ? results : results.get(1));
        }
        log.info(aMarker, "<-------Json toFile Action for {} has been completed------->" + jsonToFile.getName());
    }

    @Override
    public boolean executeIf() throws Exception {
        return jsonToFile.getCondition();
    }
}
