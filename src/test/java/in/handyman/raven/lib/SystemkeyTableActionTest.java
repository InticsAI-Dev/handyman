package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.SystemkeyTable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class SystemkeyTableActionTest {

    @Test
    void testExecute() throws Exception{
        SystemkeyTable systemkeyTable = SystemkeyTable.builder()
                .name("System key table Creation")
                .resourceConn("intics_zio_db_conn")
                .querySet("select mdc.sor_item_name,\n" +
                        " \t\tstring_agg(sk.system_key::text, ', ' order by sk.system_key DESC) as all_system_keys\n" +
                        " from config.master_data_config mdc\n" +
                        " join md_lookup_systemkey.system_key sk on sk.system_key_id = mdc.system_key_id\n" +
                        " group by mdc.sor_item_name;")
                .condition(true)
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        SystemkeyTableAction systemkeyTableAction = new SystemkeyTableAction(actionExecutionAudit,log,systemkeyTable);
        systemkeyTableAction.execute();
    }
}
