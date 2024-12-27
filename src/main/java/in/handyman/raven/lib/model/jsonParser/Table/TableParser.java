package in.handyman.raven.lib.model.jsonParser.Table;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TableParser {

    public static ObjectNode parseTables(String jsonStr) throws IOException {
        // Initialize ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        List<ObjectNode> aggregatedResult = new ArrayList<>();
        // Read the JSON string into JsonNode
        JsonNode inputJson = objectMapper.readTree(jsonStr);
        // Extract the "Tables" node
        JsonNode tableValues = inputJson.get("Tables");

        ObjectNode safeJson = (ObjectNode) objectMapper.readTree("{}");

        if (tableValues != null && !tableValues.isNull()) {

            // Deserialize the "Tables" node into a List of Table objects
            List<Table> tables = objectMapper.readValue(tableValues.toString(), new TypeReference<List<Table>>() {
            });

            ArrayNode resultArray = objectMapper.createArrayNode();

            // AtomicInteger to keep track of the index

            AtomicInteger i = new AtomicInteger(0); // Define AtomicInteger outside the loop

            tables.forEach(s -> {
                ObjectNode boundingBox = objectMapper.createObjectNode();
                boundingBox.put("x", 0);
                boundingBox.put("y", 0);
                boundingBox.put("width", 0);
                boundingBox.put("height", 0);

                ObjectNode tableNode = objectMapper.createObjectNode();
                tableNode.put("TableId", i.getAndIncrement());
                tableNode.set("Rows", objectMapper.valueToTree(s.getRows()));
                tableNode.put("confidence", 0.0F);
                tableNode.set("boundingBox", boundingBox);

                resultArray.add(tableNode);
            });

            ObjectNode jsonObject = objectMapper.createObjectNode();

            // Add the list to the JSON object with the key "key"
            jsonObject.set("tables", objectMapper.valueToTree(resultArray));
            return jsonObject;
        }else {
            // when null is occured
            return safeJson;
        }

    }
}
