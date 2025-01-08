package in.handyman.raven.lib.model.jsonParser.KVP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.Getter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Getter
public class SORParser {
    private final ActionExecutionAudit action;
    private final Map<String, String> sorItems = new HashMap<>();

    public SORParser(ActionExecutionAudit action) {
        this.action = action;
    }

    public void parseJSON(String jsonStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonStr);
            processNode(rootNode);
        } catch (Exception e) {
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("error in execute method for KVP parser action", handymanException, action);

        }
    }

    private void processNode(JsonNode node) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            if (value.isObject()) {
                // If it's an object, just recurse without building path
                processNode(value);
            } else {
                // Store just the leaf key and its value
                sorItems.put(key, value.asText());
            }
        }
    }


}