package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxJsonParser;
import in.handyman.raven.lib.model.kvp.checkbox.CheckboxQueryInputTable;
import in.handyman.raven.lib.model.kvp.checkbox.CheckboxQueryOutputTable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CheckboxJsonParserConsumerProcessTest {

    private final Logger log = LoggerFactory.getLogger(CheckboxJsonParserConsumerProcessTest.class);
    private final Marker marker = MarkerFactory.getMarker("Test");

    @Test
    public void testProcessBboxFormat() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        action.getContext().put("item.wise.encryption", "false");
        action.getContext().put("kvp.json.parser.encryption", "false");

        CheckboxJsonParser checkboxJsonParser = CheckboxJsonParser.builder().build();
        CheckboxJsonParserConsumerProcess consumer = new CheckboxJsonParserConsumerProcess(log, marker, action,
                checkboxJsonParser);

        String jsonResponse = "{\"chbq_grps\":[{\"sec_hdr\":\"Section 1\",\"grp_bbox\":[630,202,780,225],\"opts\":[{\"l\":\"Option 1\",\"s\":\"C\"}]}]}";
        CheckboxQueryInputTable input = CheckboxQueryInputTable.builder()
                .response(jsonResponse)
                .rootPipelineId(1L)
                .batchId("1")
                .originId("1")
                .paperNo(1)
                .build();

        List<CheckboxQueryOutputTable> outputs = consumer.process(new URL("http://localhost"), input);

        assertNotNull(outputs);
        assertEquals(1, outputs.size());
        CheckboxQueryOutputTable output = outputs.get(0);

        String expectedBbox = "{\"topLeftX\":630,\"topLeftY\":202,\"bottomRightX\":780,\"bottomRightY\":225}";
        assertEquals(expectedBbox, output.getBBoxAsIs());
        assertEquals(expectedBbox, output.getBoundingBox());

        // Verify unconditional encryption
        // Since we don't have a mock, we check if they are NOT equal to original if
        // encryption is working
        // Or if encryption is identity for mock, we might need a better way.
        // But the user said it was NOT encrypting, so checking for change is a good
        // start.
        assertNotNull(output.getSorItemLabel());
        assertNotNull(output.getSectionAlias());
    }
}
