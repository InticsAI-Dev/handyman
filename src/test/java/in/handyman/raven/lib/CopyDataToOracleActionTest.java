package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CopyDataToOracle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CopyDataToOracleActionTest {

    @Test
    void execute() throws Exception {
        CopyDataToOracle copyData = CopyDataToOracle.builder()
                .name("copy Data")
                .source("intics_zio_db_conn")
                .to("intics_oracle_db_conn")
                .value("INSERT INTO CONFIG.SPW_COMMON_CONFIG (VARIABLE, VALUE, ACTIVE, CREATED_BY, CREATED_DATE, LAST_MODIFIED_BY, LAST_MODIFIED_DATE, \"version\")\n" +
                        "SELECT variable, value, active, created_by, created_date, last_modified_by, last_modified_date, version FROM config.spw_common_config;")
                .fetchBatchSize("10")
                .condition(true)
                .writeBatchSize("10")
                .writeThreadCount("2")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("paper.itemizer.multipart.upload.url", "http://localhost:8002/multipart-download"),
                Map.entry("write.batch.size", "5")));

        CopyDataToOracleAction copyDataToOracleAction = new CopyDataToOracleAction(actionExecutionAudit, log, copyData);
        copyDataToOracleAction.execute();
    }
}