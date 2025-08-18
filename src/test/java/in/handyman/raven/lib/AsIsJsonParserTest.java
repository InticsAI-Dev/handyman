package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsIsJsonParserTest {

    ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void convertOcrJson() throws JsonProcessingException {
        String inputOcrString = "{ \"lines\": [" +
                "  { \"line_number\": 1, \"content\": \"QR Code\" }," +
                "  { \"line_number\": 2, \"content\": \"WELLPOINT - AMERIGROUP\" }," +
                "  { \"line_number\": 3, \"content\": \"PO BOX 61010\" }" +
                "]}";
        OcrInputJson ocrInputJson = objectMapper.readValue(inputOcrString, OcrInputJson.class);

        List<Paragraph> paragraphs = new ArrayList<>();
        List<Map<String, LineJson>> outputJsonLines = new ArrayList<>();


        ocrInputJson.lines.forEach(ocrInputLineItem -> {

            Map<String, LineJson> lineIdJson = new HashMap<>();
            lineIdJson.put("paragraph" + ocrInputLineItem.lineNumber
                    , LineJson.builder().text(ocrInputLineItem.content).build());

            outputJsonLines.add(lineIdJson);

            Paragraph paragraph = Paragraph.builder().id(ocrInputLineItem.lineNumber).lines(outputJsonLines).build();
            paragraphs.add(paragraph);

        });
        System.out.println(paragraphs);


    }

    @Test
    void convertKvpJson() throws JsonProcessingException {
        String inputKvpString = "{\n" +
                "  \"Standard Mail Cover Sheet\": {\n" +
                "    \"Company\": null,\n" +
                "    \"MCAG Company Recv'd Date & Time\": \"MAY 25 2021 11:54 AM\",\n" +
                "    \"RECEIVED\": \"MAY 25 2021\",\n" +
                "    \"MCAG Unit Recv'd Date & Time\": \"12:01 PM\",\n" +
                "    \"Scanned\": \"C\",\n" +
                "    \"ATTN\": \"Medicare Specialty\"\n" +
                "  }\n" +
                "}\n";
        OcrInputJson ocrInputJson = objectMapper.readValue(inputKvpString, OcrInputJson.class);

        List<Paragraph> paragraphs = new ArrayList<>();
        List<Map<String, LineJson>> outputJsonLines = new ArrayList<>();


        ocrInputJson.lines.forEach(ocrInputLineItem -> {

            Map<String, LineJson> lineIdJson = new HashMap<>();
            lineIdJson.put("paragraph" + ocrInputLineItem.lineNumber
                    , LineJson.builder().text(ocrInputLineItem.content).build());

            outputJsonLines.add(lineIdJson);

            Paragraph paragraph = Paragraph.builder().id(ocrInputLineItem.lineNumber).lines(outputJsonLines).build();
            paragraphs.add(paragraph);

        });
        System.out.println(paragraphs);


    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class OcrInputLineItem {
        @JsonProperty("line_number")
        private String lineNumber;
        private String content;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class OcrInputJson {
        private List<OcrInputLineItem> lines;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class OcrOutputJson {
        private List<Paragraph> paragraphs;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class Paragraph {
        private String id;
        private List<Map<String, LineJson>> lines;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class LineJson {
        private String text;
        private BoundingBox boundingBox;
        private float confidenceScore;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class BoundingBox {
        private float x;
        private float y;
        private float width;
        private float height;


    }
}
