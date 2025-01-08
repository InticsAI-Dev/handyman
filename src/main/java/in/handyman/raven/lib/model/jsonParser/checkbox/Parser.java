package in.handyman.raven.lib.model.jsonParser.checkbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Parser {
    public static String processJson (JsonNode jsonElement) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode finalResult = objectMapper.createObjectNode();
        ObjectNode output = null;
        if (jsonElement.isObject()) {
            // Iterate over the keys of the JSONObject
            Iterator<String> fieldNames = jsonElement.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode value = jsonElement.get(fieldName);
                processJson(value); // Recurse on the value

            }
        } else if (jsonElement.isArray()) {
            // If the element is a JSONArray, iterate through its elements
            output = objectMapper.createObjectNode();
            ArrayNode selectionElements = objectMapper.createArrayNode();

            List<DataParser> dataParser = objectMapper.readValue(jsonElement.toString(), new TypeReference<List<DataParser>>() {
            });


            dataParser.forEach(row -> {
                ObjectNode selectionElement = objectMapper.createObjectNode();
                selectionElement.put("Question", row.getQuestion());

                // Add options
                ArrayNode optionsArray = objectMapper.createArrayNode();
                row.getOptions().forEach(option -> {
                    ObjectNode optionObject = objectMapper.createObjectNode();
                    optionObject.put("OptionText", option.getOptionText());
                    optionObject.put("Status", option.getStatus());
                    optionsArray.add(optionObject);
                });
                selectionElement.put("Options", optionsArray);

                // Add boundingBox
                ObjectNode boundingBox = objectMapper.createObjectNode();
                boundingBox.put("x", 0);
                boundingBox.put("y", 0);
                boundingBox.put("width", 0);
                boundingBox.put("height", 0);
                selectionElement.put("boundingBox", boundingBox);

                // Add confidence
                selectionElement.put("confidence", 0);

                selectionElements.add(selectionElement);
            });

            output.set("selectionElements", selectionElements);

        }
        return output.toString();
    }
}
