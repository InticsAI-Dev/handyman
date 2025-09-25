package in.handyman.raven.lib.adapters.comparison;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.slf4j.Logger;

public interface ComparisonAdapter {
    Long validate(ControlDataComparisonQueryInputTable comparisonInputLineItem, ActionExecutionAudit action, Logger log);
    String getName();
}
