//package in.handyman.raven.lib;
//
//
//import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
//import in.handyman.raven.lib.model.ConvertExcelToDatabase;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//
//@Slf4j
//class ConvertExcelToDatabaseActionTest {
//    @Test
//    public void convertToExcel() throws Exception {
//        ConvertExcelToDatabase convertExcelToDatabase = ConvertExcelToDatabase
//                .builder()
//                .name("Convert excel to database")
//                .condition(true)
//                .fileType("Excel")
//                .resourceConn("intics_zio_db_conn")
//                .querySet("SELECT migration_id, group_id, tenant_id, file_path,root_pipeline_id\n" +
//                        "FROM meta_bootstraping.import_excel_input_table where tenant_id=1012;")
//                .targetConn("intics_zio_db_conn")
//                .build();
//
//        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
//        actionExecutionAudit.setActionId(1234L);
//        actionExecutionAudit.setRootPipelineId(1234L);
//        actionExecutionAudit.getContext().put("", "");
//        actionExecutionAudit.getContext().put("tenant_id","1");
//
//        ConvertExcelToDatabaseAction convertExcelToDatabaseAction = new ConvertExcelToDatabaseAction(actionExecutionAudit, log, convertExcelToDatabase);
//        convertExcelToDatabaseAction.execute();
//
//
//    }
//}