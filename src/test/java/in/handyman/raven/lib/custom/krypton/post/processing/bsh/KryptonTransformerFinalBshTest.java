package in.handyman.raven.lib.custom.krypton.post.processing.bsh;

import bsh.EvalError;
import bsh.Interpreter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
        Path filePath = Paths.get("/home/christopher.paulraj@zucisystems.com/Desktop/pipeline/handyman/src/main/java/in/handyman/raven/lib/custom/krypton/post/processing/bsh/KryptonTransformerFinalBsh.java");
        String fileContent = new String(Files.readAllBytes(filePath));
        Map inputJson = inputKryptonJson2();

        Interpreter interpreter = new Interpreter();
        log.info("Beanshell script evaluated successfully.");

        interpreter.eval(fileContent);
        interpreter.set("logger", log);
        String classInstantiation = "KryptonTransformerFinalBsh mapper = new KryptonTransformerFinalBsh(logger);";
        interpreter.eval(classInstantiation);
        interpreter.set("responseMap", inputJson);

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


    Map<String,Object> inputKryptonJson1(){
        Map<String,Object> inputJson = new HashMap<>();
        Map<String,Object> memberInformation = new HashMap<>();
        memberInformation.put("memberType", "");
        memberInformation.put("memberName", "");
        memberInformation.put("memberId", "");
        memberInformation.put("memberGroupId", null);
        memberInformation.put("memberDOB", "");
        memberInformation.put("memberAddress", "");
        memberInformation.put("memberCity", "");
        memberInformation.put("memberState", "");
        memberInformation.put("memberZipcode", "");
        memberInformation.put("memberPhone", null);
        memberInformation.put("medicaidId", "");
        memberInformation.put("memberGender", "null");

        Map<String,Object> serviceDetails = new HashMap<>();
        serviceDetails.put("cptCodes", Arrays.asList("", ""));
        serviceDetails.put("diagnosisCodes", new ArrayList<>());
        serviceDetails.put("authorizationID", "");
        serviceDetails.put("levelOfService", "");
        serviceDetails.put("levelOfCare", List.of(""));
        serviceDetails.put("serviceStartDate", "");
        serviceDetails.put("serviceEndDate", null);
        serviceDetails.put("admitDate", "");
        serviceDetails.put("dischargeDate", null);
        serviceDetails.put("voluntaryInvoluntaryStatus", "");

        inputJson.put("MemberInformation", Arrays.asList(memberInformation));
        inputJson.put("ServiceDetails", serviceDetails);

        return inputJson;
    }

    Map<String, Object> inputKryptonJson2() {
        Map<String, Object> memberInfo = new HashMap<>();
        memberInfo.put("memberType", "Patient");
        memberInfo.put("memberName", "John Doe");
        memberInfo.put("memberId", "M123456");
        memberInfo.put("memberGroupId", "G987654");
        memberInfo.put("memberDOB", "1985-06-15");
        memberInfo.put("memberAddress", "1234 Elm Street");
        memberInfo.put("memberCity", "Los Angeles");
        memberInfo.put("memberState", "CA");
        memberInfo.put("memberZipcode", "90001");
        memberInfo.put("memberPhone", "123-456-7890");
        memberInfo.put("medicaidId", "MD567890");
        memberInfo.put("memberGender", "Male");

        Map<String, Object> memberInfo1 = new HashMap<>();
        memberInfo1.put("memberType", "Member");
        memberInfo1.put("memberName", "John Doe");
        memberInfo1.put("memberId", "M123456");
        memberInfo1.put("memberGroupId", "G987654");
        memberInfo1.put("memberDOB", "1985-06-15");
        memberInfo1.put("memberAddress", "1234 Elm Street");
        memberInfo1.put("memberCity", "Los Angeles");
        memberInfo1.put("memberState", "CA");
        memberInfo1.put("memberZipcode", "90001");
        memberInfo1.put("memberPhone", "123-456-7890");
        memberInfo1.put("medicaidId", "MD567890");
        memberInfo1.put("memberGender", "Male");


        Map<String, Object> facilityInfo = new HashMap<>();
        facilityInfo.put("providerFacilityType", "Hospital");
        facilityInfo.put("providerFacilityName", "Cedars-Sinai Medical Center");
        facilityInfo.put("providerFacilityNPI", "1122334455");
        facilityInfo.put("providerFacilityTaxId", "99-8765432");
        facilityInfo.put("providerFacilityAddress", "8700 Beverly Blvd");
        facilityInfo.put("providerFacilityCity", "Los Angeles");
        facilityInfo.put("providerFacilityState", "CA");
        facilityInfo.put("providerFacilityZip", "90048");
        facilityInfo.put("providerFacilitySpecialty", "General Medicine");

        Map<String, Object> servicingProvider = new HashMap<>();
        servicingProvider.put("providerType", "Attending Physician");
        servicingProvider.put("providerName", "Dr. Sarah Thompson");
        servicingProvider.put("providerNPI", "1780974717");
        servicingProvider.put("providerTaxId", "382693801");
        servicingProvider.put("providerAddress", "2801 W Kinnickinnic River Pkwy Ste 250");
        servicingProvider.put("providerCity", "Milwaukee");
        servicingProvider.put("providerState", "WI");
        servicingProvider.put("providerZip", "53215-3678");
        servicingProvider.put("providerSpecialty", "Cardiology");

        Map<String, Object> serviceDetails = new HashMap<>();
        serviceDetails.put("cptCodes", Arrays.asList("99213", "85025"));
        serviceDetails.put("diagnosisCodes", Arrays.asList("I10", "E11.9"));
        serviceDetails.put("authorizationID", "AUTH123456");
        serviceDetails.put("levelOfService", "Inpatient");
        serviceDetails.put("levelOfCare", List.of("Acute Care"));
        serviceDetails.put("serviceStartDate", "2024-03-25");
        serviceDetails.put("serviceEndDate", "2024-03-30");
        serviceDetails.put("admitDate", "2024-03-25");
        serviceDetails.put("dischargeDate", "2024-03-30");
        serviceDetails.put("voluntaryInvoluntaryStatus", "Voluntary");

        Map<String, Object> inputJson = new HashMap<>();
//        inputJson.put("MemberInformation", List.of(memberInfo,memberInfo1));
        inputJson.put("MemberInformation", List.of(memberInfo1,memberInfo,memberInfo1,memberInfo1));
        inputJson.put("FacilityInformation", List.of(facilityInfo));
        inputJson.put("ServicingProviderInformation", List.of(servicingProvider));
        inputJson.put("ReferringProviderInformation", new ArrayList<>()); // Empty List
        inputJson.put("UndefinedProviderInformation", new ArrayList<>()); // Empty List
        inputJson.put("ServiceDetails", serviceDetails);

        return inputJson;
    }

    @Test
    void mapToMemberDetails() throws IOException {


        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> inputJson = objectMapper.readValue(new File("src/main/resources/memberJson.txt"), Map.class);

        Map<String, Object> jsonMap = objectMapper.readValue(new File("src/main/resources/memberJson.txt"), Map.class);

        List<Map<String, Object>> memberDetails = KryptonTransformerFinalBsh.mapToMemberDetails(jsonMap);

        System.out.println(memberDetails.toString());
    }


}
