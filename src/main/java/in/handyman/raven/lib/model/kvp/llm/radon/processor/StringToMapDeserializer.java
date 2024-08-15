package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class StringToMapDeserializer extends JsonDeserializer<Map<String, Object>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String json = jsonParser.getText();
        // Remove the ```json block and any leading/trailing spaces
        json = json.replace("```json", "").replace("```", "").trim();
        // Deserialize the cleaned JSON string into a Map
        return objectMapper.readValue(json, Map.class);
    }
}

