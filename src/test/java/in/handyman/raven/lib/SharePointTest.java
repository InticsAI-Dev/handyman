package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.process.HRequestResolver;
import in.handyman.raven.lambda.process.LContext;
import in.handyman.raven.lambda.process.LambdaEngine;
import in.handyman.raven.util.InstanceUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SharePointTest {
    // urgency trige
    @Test
    void execute() {
        LambdaEngine.start(LContext.builder()
                .pipelineName("smoke.test")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .build());
    }

    @Test
    void testRegWithHyphen() {
        final String PHONE_NUMBER_REGEX = "^\\(?(\\d{3})\\)?[-]?(\\d{3})[-]?(\\d{4})$";
        final String NUMBER_REGEX = "^[+-]?(\\d+\\.?\\d*|\\.\\d+)$";
        Pattern pattern = Pattern.compile(PHONE_NUMBER_REGEX);
        Matcher matcher = pattern.matcher("098-289-0948");
        boolean matchScore = matcher.matches();
        int result = matchScore ? 70 : 0;

    }

    @Test
    void testRegWithBracket() {
        final String PHONE_NUMBER_REGEX = "^\\(?(\\d{3})\\)?[-]?(\\d{3})[-]?(\\d{4})$";
        final String NUMBER_REGEX = "^[+-]?(\\d+\\.?\\d*|\\.\\d+)$";
        Pattern pattern = Pattern.compile(PHONE_NUMBER_REGEX);
        Matcher matcher = pattern.matcher("(098)289-0948");
        boolean matchScore = matcher.matches();
        int result = matchScore ? 70 : 0;

    }

    @Test
    void testRegWithNumeric() {
        final String NUMBER_REGEX = "^[+-]?(\\d+\\.?\\d*|\\.\\d+)$";
        Pattern pattern = Pattern.compile(NUMBER_REGEX);
        Matcher matcher = pattern.matcher("JODISANDERS");
        boolean matchScore = matcher.matches();
        int result = matchScore ? 70 : 0;

    }

    @Test
    void testRegWithwStringAndNumber() {
        final String NAME_NUMBER_REGEX = "^(.+?)\\s*(\\d+)$";
        Pattern pattern = Pattern.compile(NAME_NUMBER_REGEX);
        Matcher matcher = pattern.matcher("Rebbecca JohnH123847494");
        if (matcher.matches()) {
            String name = matcher.group(1);
            String number = matcher.group(2);
            System.out.println("Name: " + name);
            System.out.println("Number: " + number);
        } else {
            System.out.println("No match found.");
        }
    }

    @Test
    void testStringArrayConfig(){
        String []restrictedAnswers = new String[]{"reg,efmj,cin"};
        System.out.println(restrictedAnswers);
    }

    @Test
    void executeTest() {
        String test = "15/09-2023";
        test = test.replaceAll("[-/]", "_");
        System.out.println(test);
    }

    @Test
    void exceptionOccur() {
     /*   ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        try {
            ActionExecutionErrorAudit audit = ActionExecutionErrorAudit.builder()
                    .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                    .actionId(actionExecutionAudit.getActionId())
                    .build();
        } catch (Exception ex) {

            throw new HandymanException(ex.toString());
        }*/
    }


    @Test
    void docnetResult() throws JsonProcessingException {

        String jsonString = "[{\"sorKey\":\"date_of_birth\",\"sorId\":\"12\",\"attributionResult\":[{\"question\":\"Date of Birth\",\"predictedAttributionValue\":\"12/16/1963\",\"confidence_score\":\"\"}]},{\"sorKey\":\"date_of_birth\",\"sorId\":\"12\",\"attributionResult\":[{\"question\":\"Date of Birth\",\"predictedAttributionValue\":\"12/16/1963\",\"confidence_score\":\"\"}]},{\"sorKey\":\"date_of_birth\",\"sorId\":\"12\",\"attributionResult\":[{\"question\":\"Date of Birth\",\"predictedAttributionValue\":\"12/16/1963\",\"confidence_score\":\"\"}]},{\"sorKey\":\"date_of_birth\",\"sorId\":\"12\",\"attributionResult\":[{\"question\":\"Date of Birth\",\"predictedAttributionValue\":\"12/16/1963\",\"confidence_score\":\"\"}]}]";

        JSONArray jObj = new JSONArray(jsonString);

        jObj.forEach(resultObject -> {
            JSONObject obj = (JSONObject) resultObject;
            obj.getString("sorKey");
            obj.getString("sorId");
            JSONArray result = obj.getJSONArray("attributionResult");
            for (int i = 0; i < result.length(); i++) {
                JSONObject object = (JSONObject) result.get(i);
                object.getString("question");
                object.getString("predictedAttributionValue");
                System.out.println(object.getString("question"));
                System.out.println(object.getString("predictedAttributionValue"));

            }
        });


    }

    @Data
    @AllArgsConstructor
    class DocnetQuestions {
        private JsonNode question;
    }

    @Test
    void TestDocExtractDelivery() throws MalformedURLException {

        final ObjectMapper MAPPER = new ObjectMapper();
        final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
        final OkHttpClient httpclient = InstanceUtil.createOkHttpClient();

        final ObjectNode objectNode = MAPPER.createObjectNode();
        objectNode.put("documentId", "449e941a-4e22-46fe-b1a0-6cb9ef339d36");
        objectNode.put("inticsZipUri", "https://agadia.intics.ai/downloadOutboundZip/449e941a-4e22-46fe-b1a0-6cb9ef339d36");
        objectNode.put("checksum", "90456185F32217DA8055E40716E590BB");
        Request request = new Request.Builder().url("https://fbmltprd.rxcontinuum.com/docextraction/api/Inbounddocument_in/docextractdeliverynotify")
                .header("appId", "285089d1-7bb5-4db2-a971-b888403939b7")
                .header("appKeyId", "Y9p4SZmV5XhkBeOqFeuBgVwU1Np2jGZP2SpQpamKGru66GsD0hSovWBemKsrORoz")
                .post(RequestBody.create(objectNode.toString(), MediaTypeJSON))
                .build();

        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                //log.info(aMarker, "Sent response for the document {}", documentId);
            } else {
                throw new HandymanException(responseBody);
            }
        } catch (Throwable t) {
            //log.error(aMarker, "error inserting row {}", result, t);
        }

    }

}
