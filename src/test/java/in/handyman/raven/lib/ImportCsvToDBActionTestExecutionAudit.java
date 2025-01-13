package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import in.handyman.raven.lib.model.ImportCsvToDB;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

class ImportCsvToDBActionTestExecutionAudit {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    void execute() throws Exception {

        final ImportCsvToDBAction importCsvToDBAction = new ImportCsvToDBAction(ActionExecutionAudit.builder().build(),
                logger, ImportCsvToDB.builder()
                .condition(true)
                .tableName("CCCU_DNA.dbo.MOCK_DATA")
                .value(Collections.singletonList("/home/shankar.t@zucisystems.com/Downloads/MOCK_DATA.csv"))
                .batchSize("99")
                .writeThreadCount("3")
                .target("")
                .build());

        importCsvToDBAction.execute();
    }


}
