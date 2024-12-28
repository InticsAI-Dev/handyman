package in.handyman.raven.lib.model.jsonParser.checkbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Parser {
    public static String processJson (Object jsonElement) throws IOException {
        JSONObject finalResult = new JSONObject();
        ObjectMapper objectMapper = new ObjectMapper();
        if (jsonElement instanceof JSONObject) {
            // Iterate over the keys of the JSONObject
            JSONObject jsonObject = (JSONObject) jsonElement;
            jsonObject.keySet().forEach(key -> {
                Object value = jsonObject.get(key);
                try {
                    processJson(value); // Recurse on the value
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (jsonElement instanceof JSONArray) {
            // If the element is a JSONArray, iterate through its elements

            JSONArray selectionElements = new JSONArray();

            List<DataParser> dataParser = objectMapper.readValue(jsonElement.toString(), new TypeReference<List<DataParser>>() {
            });


            dataParser.forEach(row -> {
                JSONObject selectionElement = new JSONObject();
                selectionElement.put("Question", row.getQuestion());

                // Add options
                JSONArray optionsArray = new JSONArray();
                row.getOptions().forEach(option -> {
                    JSONObject optionObject = new JSONObject();
                    optionObject.put("OptionText", option.getOptionText());
                    optionObject.put("Status", option.getStatus());
                    optionsArray.put(optionObject);
                });
                selectionElement.put("Options", optionsArray);

                // Add boundingBox
                JSONObject boundingBox = new JSONObject();
                boundingBox.put("x", 0);
                boundingBox.put("y", 0);
                boundingBox.put("width", 0);
                boundingBox.put("height", 0);
                selectionElement.put("boundingBox", boundingBox);

                // Add confidence
                selectionElement.put("confidence", 0);

                selectionElements.put(selectionElement);
            });

            finalResult.put("selectionElements", selectionElements);

        }
    return finalResult.toString();
    }
}
