package in.handyman.raven.lib.model.jsonParser.checkbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.lib.model.jsonParser.Bbox;
import in.handyman.raven.lib.model.jsonParser.checkbox.CheckBoxFinal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CheckboxParser {
    public static String processJson (JsonNode jsonElement) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode output = null;
        String jsonResponseStr="";
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
            List<CheckboxContentNode> selectionElements = new ArrayList<>();
            // parsing the values of array with pojo
            List<DataParser> dataParser = objectMapper.readValue(jsonElement.toString(), new TypeReference<List<DataParser>>() {
            });

            dataParser.forEach(row -> {
                // creating an empty list of object
                List<CheckboxStatus> optionsArray = new ArrayList<>();

                row.getOptions().forEach(option -> {

                    CheckboxStatus checkboxStatus = new CheckboxStatus();

                    optionsArray.add(checkboxStatus);
                });


                Bbox bBox = new Bbox();
                bBox.setX(0); bBox.setY(0); bBox.setWidth(0); bBox.setHeight(0);

                CheckboxContentNode checkboxContentNode = new CheckboxContentNode();
                checkboxContentNode.setBoundingBox(bBox);
                checkboxContentNode.setConfidence(0.0F);
                checkboxContentNode.setOptions(row.getOptions());
                checkboxContentNode.setQuestion(row.getQuestion());

                selectionElements.add(checkboxContentNode);
            });

//            ArrayNode selectionElementsArrayNode = objectMapper.valueToTree(selectionElements);
//            output.set("selectionElements", selectionElements);
            CheckBoxFinal checkBoxFinal = new CheckBoxFinal();
            checkBoxFinal.setSelectionElements(selectionElements);
            ObjectMapper checkBoxParser = new ObjectMapper();
            jsonResponseStr = checkBoxParser.writeValueAsString(checkBoxFinal);


        }
        return jsonResponseStr;
    }
}
