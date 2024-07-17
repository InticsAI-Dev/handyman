package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TritonModelLoadUnload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class TritonModelLoadUnloadActionTest {

    @Test
    void tritonModelLoad() throws Exception {

        TritonModelLoadUnload tritonModelLoadUnload = TritonModelLoadUnload.builder()
                .name("Triton model Loading")
                .resourceConn("intics_zio_db_conn")
                .endPoint("http://localhost:8200/v2/repository/models/auto-rotator-service/load")
                .configVariable("AUTO_ROTATION_CONFIG_JSON")
                .loadType("load")
                .condition(true)
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.handwritten.url", "http://copro.valuation:10189/copro/attribution/kvp-docnet");
        actionExecutionAudit.getContext().put("okhttp.client.timeout", "20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id", "1");
        actionExecutionAudit.setProcessId(138980079308730208L);

        TritonModelLoadUnloadAction tritonModelLoadUnloadAction = new TritonModelLoadUnloadAction(actionExecutionAudit, log, tritonModelLoadUnload);
        tritonModelLoadUnloadAction.execute();

    }

    @Test
    void tritonModelUnLoad() throws Exception {

        TritonModelLoadUnload tritonModelLoadUnload = TritonModelLoadUnload.builder()
                .name("Triton model Loading")
                .resourceConn("intics_zio_db_conn")
                .endPoint("http://localhost:8200/v2/repository/models/auto-rotator-service/unload")
                .configVariable("AUTO_ROTATION_CONFIG_JSON")
                .loadType("unload")
                .condition(true)
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.handwritten.url", "http://copro.valuation:10189/copro/attribution/kvp-docnet");
        actionExecutionAudit.getContext().put("okhttp.client.timeout", "20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id", "1");
        actionExecutionAudit.setProcessId(138980079308730208L);

        TritonModelLoadUnloadAction tritonModelLoadUnloadAction = new TritonModelLoadUnloadAction(actionExecutionAudit, log, tritonModelLoadUnload);
        tritonModelLoadUnloadAction.execute();

    }

}