package in.handyman.raven.lib.model.jsonParser.Table;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.lib.model.jsonParser.Bbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TableParser {

    public static ObjectNode parseTables(String jsonStr) throws IOException {
        // Initialize ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();


        // Read the JSON string into JsonNode
        JsonNode inputJson = objectMapper.readTree(jsonStr);
        // Extract the "Tables" node
        JsonNode tableValues = inputJson.get("Tables");

        ArrayNode safeJson = objectMapper.createArrayNode(); // Correctly initialize as ArrayNode

        if (tableValues != null && !tableValues.isNull()) {

            // Deserialize the "Tables" node into a List of Table objects
            List<Table> tables = objectMapper.readValue(tableValues.toString(), new TypeReference<List<Table>>() {
            });

            List<TableContentNode> resultArray = new ArrayList<>();  // List to hold the results

            // AtomicInteger to keep track of the index

            AtomicInteger i = new AtomicInteger(0); // Define AtomicInteger outside the loop

            tables.forEach(s -> {
                Bbox bBox = new Bbox();
                bBox.setX(0); bBox.setY(0); bBox.setWidth(0); bBox.setHeight(0);

                TableContentNode tableContent = new TableContentNode();
                tableContent.setTableId(i.getAndIncrement());
                tableContent.setRows(objectMapper.valueToTree(s.getRows()));
                tableContent.setConfidence(0.0F);
                tableContent.setBBox(bBox);
                resultArray.add(tableContent);
            });
            JsonNode finalResult = objectMapper.valueToTree(resultArray);


            // Add the list to the JSON object with the key "key"
            return objectNode.set("tables", finalResult);
        }else {
            // when null is occurred
            return objectNode.set("tables", safeJson);
        }

    }
}
