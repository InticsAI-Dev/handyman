package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;

import java.util.Collections;
import java.util.Objects;

public class TritonRequestProcessor {

    private static final String ENCRYPTION_CHAIN_ACTIVATOR = "encryption.pipeline.activator";
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static EncryptionService encryptionService = new EncryptionService();

    public TritonRequestProcessor(ObjectMapper objectMapper, EncryptionService encryptionService) {
        TritonRequestProcessor.objectMapper = objectMapper;
        TritonRequestProcessor.encryptionService = encryptionService;
    }

    public static String processTritonRequest(TritonInputRequest tritonInputRequest, ActionExecutionAudit action) throws Exception {
        TritonRequest requestBody = (TritonRequest) tritonInputRequest.getInputs().get(0);

        // Extract the data array from the TritonRequest
        ArrayNode dataArray = objectMapper.valueToTree(requestBody.getData());

        // Check if encryption is activated
        String encryptionPipelineActivator = action.getContext().get(ENCRYPTION_CHAIN_ACTIVATOR);
        if (Objects.equals("true", encryptionPipelineActivator)) {
            // Encrypt only the content of each data item
            for (int i = 0; i < dataArray.size(); i++) {
                if (dataArray.get(i).isTextual()) {
                    String jsonData = dataArray.get(i).asText();
                    String encryptedData = encryptionService.encryptData(jsonData);
//                    dataArray.set(i, TextNode.valueOf(encryptedData));
                    // Step 3: Parse the encrypted response to extract the cipherText field
                    JsonNode encryptedJson = objectMapper.readTree(encryptedData);
                    String cipherText = encryptedJson.get("cipherText").toString();
                    // Step 4: Replace the original text in dataArray with the cipherText
                    dataArray.set(i, TextNode.valueOf(cipherText));
                }
            }
        }

        // Create a new TritonRequest with the potentially encrypted data
        TritonRequest newRequestBody = new TritonRequest(
                requestBody.getName(),
                requestBody.getShape(),
                requestBody.getDatatype(), Collections.singletonList(dataArray)
        );

        // Set the new request body to the TritonInputRequest
        tritonInputRequest.setInputs(Collections.singletonList(newRequestBody));

        // Serialize the entire TritonInputRequest to JSON
        return objectMapper.writeValueAsString(tritonInputRequest);
    }
}