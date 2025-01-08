package in.handyman.raven.lib.ganda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class JsonParserGanda {


    @Test
    public void generateJson() throws Exception {

        File formElementJsonFile = new File("input/20250103T172001385.json");

        List<PageInfo> list = parseJsonFromList(formElementJsonFile);
        GrievanceAppeals grievanceAppeals = new GrievanceAppeals();


        Metadata grievanceAppealsMetaData = getMetadata();


        /// last json
        grievanceAppeals.setStatus("SUCCESS");
        grievanceAppeals.setRequestTxnId("20250103T172001385");
        grievanceAppeals.setTransactionId("ITX-1002");
        grievanceAppeals.setMetadata(grievanceAppealsMetaData);

        grievanceAppeals.setPageInfo(list);

//      List<GrievanceAppeals> grievanceAppeals1=new ArrayList<>();
//        grievanceAppeals1.add(grievanceAppeals);

        ObjectMapper objectMapper = new ObjectMapper();

        // Convert the grievanceAppeals object to a JSON string
        String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(grievanceAppeals);

        // Print the resulting JSON string
        System.out.println(jsonString);

    }


    private static Metadata getMetadata() {
        Metadata grievanceAppealsMetaData = Metadata.builder()
                .pageCount(2)
                .documentId("20250103T172001385.pdf")
                .documentType("GRIEVANCE_APPEALS")
                .build();
        return grievanceAppealsMetaData;
    }

    private static List<FormElement> getForm() {
        List<FormElement> formElement = new ArrayList<>();


        return formElement;
    }


    public static List<FormElement> parseJsonToFormElements(String jsonFile) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        List<FormElement> formElements = new ArrayList<>();

        parseNode("", rootNode, formElements);

        return formElements;
    }


    private static void parseNode(String parentKey, JsonNode node, List<FormElement> formElements) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                parseNode(key, field.getValue(), formElements);
            }
        } else if (node.isValueNode()) {
            formElements.add(FormElement.builder().key(parentKey).value(node.asText()).boundingBox(new BoundingBox()).build());


        }
    }

    public static List<PageInfo> parseJsonFromList(File jsonFile) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootArray = objectMapper.readTree(jsonFile);

        List<PageInfo> parsedResults = new ArrayList<>();

        // Iterate through the list of nodes
        if (rootArray.isArray()) {
            for (JsonNode node : rootArray) {
                PageInfo pageInfo = new PageInfo();
                pageInfo.setPageNumber(node.get("page_number").asInt());

                // Parse the "kvp" field into a Form object
                JsonNode kvpNode = node.get("kvp");
                if (kvpNode != null && kvpNode.isObject()) {
                    Form form = parseForm(kvpNode);
                    pageInfo.setForm(form);
                } else {
                    pageInfo.setForm(null); // Handle the case where "kvp" might be null
                }

                // Parse the "tables" field
                JsonNode tablesNode = node.get("tables");
                if (tablesNode != null && tablesNode.isArray()) {
                    List<Table> tablesData = parseTables(tablesNode);
                    pageInfo.setTables(tablesData);
                } else {
                    pageInfo.setTables(Collections.emptyList());
                }

                // Parse the "checkbox" field
                JsonNode checkboxNode = node.get("checkbox");
                if (checkboxNode != null && checkboxNode.isArray()) {
                    List<SelectionElement> checkboxData = parseCheckboxJson(checkboxNode.toString());
                    pageInfo.setSelectionElements(checkboxData);
                } else {
                    pageInfo.setSelectionElements(Collections.emptyList());
                }

                // Parse the "Text" field into TextLine objects
                JsonNode textNode = node.get("Text");
                if (textNode != null) {
                    List<TextLine> textLines = parseTextLines(textNode);
                    pageInfo.setTextLines(textLines);
                } else {
                    pageInfo.setTextLines(Collections.emptyList());
                }

                // Add the PageInfo object to the results list
                parsedResults.add(pageInfo);
            }
        }

        return parsedResults;
    }

    private static Form parseForm(JsonNode kvpNode) throws Exception {
        Form form = new Form();
        List<FormElement> formElements = parseJsonToFormElements(kvpNode.toString());
        form.setFormElements(formElements);
        return form;
    }


    private static List<TextLine> parseTextLines(JsonNode textNode) {
        List<TextLine> textLines = new ArrayList<>();

        // Ensure that we are dealing with the "lines" array in the "Text" field
        JsonNode linesNode = textNode.get("lines");
        if (linesNode != null && linesNode.isArray()) {
            for (JsonNode lineNode : linesNode) {
                TextLine textLine = new TextLine();

                // Set text content
                textLine.setText(lineNode.get("content").asText());

                // Set default confidence score
                textLine.setConfidence(0.0);

                // Set default bounding box (assuming BoundingBox is a class with default constructor)
                textLine.setBoundingBox(new BoundingBox());

                // Add the textLine object to the list
                textLines.add(textLine);
            }
        }

        return textLines;
    }


    private static List<Table> parseTables(JsonNode tablesNode) {
        List<Table> tables = new ArrayList<>();

        for (JsonNode tableNode : tablesNode) {
            Table table = new Table();

            // Parse "Rows" field
            List<Map<String, String>> rows = new ArrayList<>();
            JsonNode rowsNode = tableNode.get("Rows");
            if (rowsNode != null && rowsNode.isArray()) {
                for (JsonNode rowNode : rowsNode) {
                    Map<String, String> row = new HashMap<>();
                    rowNode.fields().forEachRemaining(entry -> row.put(entry.getKey(), entry.getValue().asText()));
                    rows.add(row);
                }
            }
            table.setRows(rows);
            table.setBoundingBox(new BoundingBox());

            tables.add(table);
        }

        return tables;
    }

    public static List<SelectionElement> parseCheckboxJson(String jsonFile) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootArray = objectMapper.readTree(jsonFile);

        List<SelectionElement> selectionElements = new ArrayList<>();

        if (rootArray.isArray()) {
            for (JsonNode node : rootArray) {
                SelectionElement selectionElement = new SelectionElement();

                // Parse the "Question" field
                selectionElement.setQuestion(node.get("Question").asText());

                // Parse the "Options" field and convert it into a list of Option objects
                JsonNode optionsNode = node.get("Options");
                List<Option> options = new ArrayList<>();
                if (optionsNode != null && optionsNode.isArray()) {
                    for (JsonNode optionNode : optionsNode) {
                        Option option = new Option();
                        option.setOptionText(optionNode.get("OptionText").asText());
                        option.setStatus(optionNode.get("Status").asText());
                        options.add(option);
                    }
                }
                selectionElement.setOptions(options);

                selectionElement.setBoundingBox(new BoundingBox());
                // Add the selection element to the list
                selectionElements.add(selectionElement);
            }
        }

        return selectionElements;
    }

}
