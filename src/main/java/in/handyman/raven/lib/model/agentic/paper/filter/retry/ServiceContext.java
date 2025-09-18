package in.handyman.raven.lib.model.agentic.paper.filter.retry;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.agentic.paper.filter.copro.CoproRetryErrorAuditTable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServiceContext {

    private final Map<String, String> contextData;

    // Optional audit objects
    private CoproRetryErrorAuditTable audit;
    private ActionExecutionAudit actionAudit;

    // Optional request/response payloads
    private String requestBody;
    private String responseBody;

    public ServiceContext(Map<String, String> contextData) {
        this.contextData = contextData != null
                ? Collections.unmodifiableMap(new HashMap<>(contextData))
                : Collections.emptyMap();
    }

    public String get(String key) {
        return contextData.get(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        return contextData.getOrDefault(key, defaultValue);
    }

    public Map<String, String> getAll() {
        return contextData;
    }

    public CoproRetryErrorAuditTable getAudit() {
        return audit;
    }

    public ServiceContext withAudit(CoproRetryErrorAuditTable audit) {
        this.audit = audit;
        return this;
    }

    public ActionExecutionAudit getActionAudit() {
        return actionAudit;
    }

    public ServiceContext withActionAudit(ActionExecutionAudit actionAudit) {
        this.actionAudit = actionAudit;
        return this;
    }

    public boolean hasAudit() {
        return audit != null;
    }

    public boolean hasActionAudit() {
        return actionAudit != null;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public ServiceContext withRequestBody(String requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public ServiceContext withResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }
}
