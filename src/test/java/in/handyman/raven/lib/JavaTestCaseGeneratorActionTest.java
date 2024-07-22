package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.JavaTestCaseGenerator;
import com.github.javaparser.JavaParser;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.reflections.Reflections.log;

class JavaTestCaseGeneratorActionTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void execute() throws Exception {

        new JavaClassMap();
        List<JavaClassMap> javaClassMapList = List.of(JavaClassMap.builder()
                        .className("AssetService")
                        .methods(List.of("uploadAssetToCopro(final File file,final Long tenantId,final String transactionId,final Long channelId,final Long userId)", "save(final Asset asset, Long userId)"))
                .build());

//        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON

        String classInfo = mapper.writeValueAsString(javaClassMapList);


        System.out.println(classInfo);

        String querySet = "SELECT 'NPOS' as document_type, 'JAVA' as project_type, '/home/manikandan.tm@zucisystems.com/workspace/alchemy/src/main/java/' as folder_path, 1 as tenant_id, '" + classInfo + "' as class_values, 1 as group_id, 'sor_meta_code' as question_verse_schema;";
        System.out.println(querySet);
        JavaTestCaseGenerator javaTestCaseGenerator = JavaTestCaseGenerator.builder()
                .name("Java Test Case Generator")
                .querySet(querySet)
                .outputTable("test_case_details.project_details")
                .condition(true)
                .resourceConn("intics_agadia_db_conn")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("write.batch.size","5"),
                Map.entry("codeGen.thread.count","1"),
                Map.entry("copro.codeGen.url","http://192.168.10.240:10009/chat/codestral?instruction=&prompt=")));

        JavaTestCaseGeneratorAction javaTestCaseGeneratorAction = new JavaTestCaseGeneratorAction(actionExecutionAudit, log, javaTestCaseGenerator);
        javaTestCaseGeneratorAction.execute();

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JavaClassMap {

        private String className;
        private List<String> methods;
    }

    @Test
    void methodTest() throws IOException {
//        List<String> lines = Files.readAllLines(Paths.get("/home/manikandan.tm@zucisystems.com/workspace/alchemy/src/main/java/org/intics/alchemy/core/info/service/AssetService.java"));
//        List<String> methodNames = extractMethodsWithParameters(lines);
//        System.out.println(methodNames);
        FileInputStream in = new FileInputStream("/home/manikandan.tm@zucisystems.com/workspace/alchemy/src/main/java/org/intics/alchemy/core/info/repo/TransactionInfosRepo.java");

        // Parse the file
        // Create an instance of JavaParser
        JavaParser javaParser = new JavaParser();

        // Parse the file
        CompilationUnit cu = javaParser.parse(in).getResult().orElseThrow(() -> new FileNotFoundException("Could not parse the file"));


        // Create a MethodVisitor instance
        MethodVisitor methodVisitor = new MethodVisitor();

        // Visit methods
        methodVisitor.visit(cu, null);

        // Get the method signatures
        List<String> methodSignatures = methodVisitor.getMethodSignatures();

        // Print the method signatures
        methodSignatures.forEach(System.out::println);


    }

    private List<String> extractMethodsWithParameters(List<String> lines) {
        return lines.stream()
                .filter(line -> line.contains("public") || line.contains("private") || line.contains("protected"))
                .filter(line -> line.contains("(") && line.contains(")") && line.contains("{"))
                .map(line -> {
                    int start = line.lastIndexOf(' ', line.indexOf('(')) + 1;
                    int end = line.indexOf('{');
                    return line.substring(start, end).trim();
                })
                .collect(Collectors.toList());
    }

    @Getter
    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        private final List<String> methodSignatures = new ArrayList<>();

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            methodSignatures.add(getMethodSignature(md));
        }

        private String getMethodSignature(MethodDeclaration md) {
            StringBuilder methodSignature = new StringBuilder();
            methodSignature.append(md.getName())
                    .append("(");

            md.getParameters().forEach(parameter -> {
                methodSignature.append(parameter.getType())
                        .append(" ")
                        .append(parameter.getName())
                        .append(", ");
            });

            // Remove the trailing comma and space if parameters exist
            if (!md.getParameters().isEmpty()) {
                methodSignature.setLength(methodSignature.length() - 2);
            }

            methodSignature.append(")");

            return methodSignature.toString();
        }

    }
}