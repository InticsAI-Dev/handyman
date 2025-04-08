package in.handyman.raven.lib.custom.krypton.post.processing.bsh;

import bsh.EvalError;
import bsh.Interpreter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class KryptonTransformerFinalBshTest {

    @Test
    void processKryptonJsonBsh() throws IOException, EvalError {
        Path filePath = Paths.get("src/main/java/in/handyman/raven/lib/custom/krypton/post/processing/bsh/KryptonTransformerFinalBsh.java");
        String fileContent = new String(Files.readAllBytes(filePath));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(new File("src/main/resources/input-json/sample.json"), Map.class);

        Interpreter interpreter = new Interpreter();
        log.info("Beanshell script evaluated successfully.");

        interpreter.eval(fileContent);
        interpreter.set("logger", log);
        String classInstantiation = "KryptonTransformerFinalBsh mapper = new KryptonTransformerFinalBsh(logger);";
        interpreter.eval(classInstantiation);
        interpreter.set("responseMap", jsonMap);

        interpreter.eval("outputJsonMap = mapper.processKryptonJson(responseMap);");

        Object outputJsonMap = interpreter.get("outputJsonMap");
        if (outputJsonMap instanceof List) {
            List<?> outputList = (List<?>) outputJsonMap; // Cast to List
            int size = outputList.size(); // Get size
            System.out.println("Size of outputJsonMap: " + size);
            ((List<?>) outputJsonMap).forEach(object -> {
                System.out.println(object);
            });
        } else if (outputJsonMap instanceof Map) {
            Map<String,Object> map=(Map<String, Object>) outputJsonMap;
            map.forEach((s, object) -> {
                System.out.println("\nContainer Name : "+s +" \nOutputs : "+object);
            });

        }


    }


    @Test
    void mapToMemberDetails() throws IOException {


        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> inputJson = objectMapper.readValue(new File("src/main/resources/memberJson.txt"), Map.class);
        JSONArray vals = new JSONArray();
        Map<String, Object> jsonMap = objectMapper.readValue(new File("src/main/resources/memberJson.txt"), Map.class);
        Object memberInformation = jsonMap.get("MemberInformation");
        // If it's an instance of List (such as ArrayList), you can cast it to a List and then create a JSONArray
        if (memberInformation instanceof List) {
            vals = new JSONArray((List<?>) memberInformation);
        }
        List<Map<String, Object>> memberDetails = KryptonTransformerFinalBsh.mapTopritorityCheckDetails("MEMBER_DETAILS",vals);

        System.out.println(memberDetails.toString());
    }


    @Test
    void processKryptonJsonTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONArray vals = new JSONArray();
        Map<String, Object> jsonMap = objectMapper.readValue(new File("/home/christopher.paulraj@zucisystems.com/Desktop/pipeline/handyman/src/main/resources/containerJson.txt"), Map.class);

         KryptonTransformerFinalBsh.processKryptonJson(jsonMap);
    }

}
