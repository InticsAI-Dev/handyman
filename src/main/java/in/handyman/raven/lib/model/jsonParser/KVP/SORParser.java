package in.handyman.raven.lib.model.jsonParser.KVP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.Getter;

import java.util.*;

@Getter
public class SORParser {
    private final ActionExecutionAudit action;
    private final Map<String, String> sorItems = new LinkedHashMap<>();

    public SORParser(ActionExecutionAudit action) {
        this.action = action;
    }

    public List<JsonNode> parseJSON(String jsonStr) {
        List<JsonNode> processedKVP = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonStr);
            processedKVP =  processNode(rootNode);
        } catch (Exception e) {
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("error in execute method for KVP parser action", handymanException, action);

        }
        return processedKVP;
    }

    private List<JsonNode> processNode(JsonNode node) {

        List<JsonNode> extractedValue = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        ObjectMapper objectMapper = new ObjectMapper();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            if (value.isObject()) {

                // If it's an object, just recurse without building path
                extractedValue.add(value);
                processNode(value);
            } else if (value.isArray()) {

                for (JsonNode element : value) {
                    extractedValue.add(element);
                }
//            } else {
//                // Store just the leaf key and its value
//                sorItems.put(key, value.asText());
//                extractedValue.add(sorItems);
            }
        }

        return extractedValue;
    }


}