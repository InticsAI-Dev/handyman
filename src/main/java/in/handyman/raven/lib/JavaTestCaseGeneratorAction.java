package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.JavaTestCaseGenerator;
import in.handyman.raven.util.InstanceUtil;
import lombok.*;
import okhttp3.*;
import org.apache.commons.text.StringSubstitutor;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "JavaTestCaseGenerator"
)
public class JavaTestCaseGeneratorAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final JavaTestCaseGenerator javaTestCaseGenerator;

    private final Marker aMarker;
    private final OkHttpClient httpclient = InstanceUtil.createOkHttpClient();

    public JavaTestCaseGeneratorAction(final ActionExecutionAudit action, final Logger log,
                                       final Object javaTestCaseGenerator) {
        this.javaTestCaseGenerator = (JavaTestCaseGenerator) javaTestCaseGenerator;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" JavaTestCaseGenerator:" + this.javaTestCaseGenerator.getName());
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute() throws Exception {

        String javaTestCaseGeneratorName = javaTestCaseGenerator.getName();
        log.info(aMarker, "Java TestCase Generator Action for {} has been started", javaTestCaseGeneratorName);

        String resourceConn = javaTestCaseGenerator.getResourceConn();
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(resourceConn);

        String endPoint = action.getContext().get("copro.codeGen.url");
        final List<URL> urls = Optional.ofNullable(endPoint).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
            try {
                return new URL(s1);
            } catch (MalformedURLException e) {
                log.error("Error in processing the URL ", e);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList())).orElse(Collections.emptyList());

        JavaTestCaseQueryResult javaTestCaseQueryResult = jdbi.withHandle(handle -> handle.createQuery(javaTestCaseGenerator.getQuerySet()).mapToBean(JavaTestCaseQueryResult.class).one());
        String projectFolderPath = javaTestCaseQueryResult.getFolderPath();

        String documentType = javaTestCaseQueryResult.getDocumentType();
        String projectType = javaTestCaseQueryResult.getProjectType();

        String classValues = javaTestCaseQueryResult.getClassValues();

        List<JavaClassMap> javaClassMapList = mapper.readValue(classValues, new TypeReference<>() {
        });

        Long groupId = javaTestCaseQueryResult.getGroupId();
        Long tenantId = javaTestCaseQueryResult.getTenantId();
        String questionVerseSchema = javaTestCaseQueryResult.getQuestionVerseSchema();

        String outputTable = javaTestCaseGenerator.getOutputTable();

        Path projectDir = Paths.get(projectFolderPath);
        List<Channel> channels = new ArrayList<>();
        getChannelDetails(projectDir, channels, jdbi, tenantId, questionVerseSchema);

        int batchSize = Integer.parseInt(action.getContext().get("codeGen.thread.count"));
        final ExecutorService executorService = Executors.newFixedThreadPool(batchSize);
        final CountDownLatch countDownLatch = getCountDownLatch(channels, javaClassMapList, urls);
        log.info("Total consumers {}", countDownLatch.getCount());

        if (!urls.isEmpty()) {
            channels.forEach(channel -> {
                String packageName = channel.getPackageName();
                List<SIP> classInfos = channel.getSips();

                classInfos.forEach(sip -> {
                    String className = sip.getClassName();

                    javaClassMapList.stream()
                            .filter(javaClassMap -> compareIgnoringWhitespace(javaClassMap.getClassName(), className))
                            .forEach(javaClassMap -> {

                                log.info(aMarker, "Started the test case generation for class: {}", className);

                                List<Synonym> methodsName = sip.getSynonyms();
                                methodsName.forEach(methodName -> {
                                    String method = methodName.getMethodName();
                                    List<String> mapMethods = javaClassMap.getMethods();
                                    mapMethods.stream()
                                            .filter(s -> compareIgnoringWhitespace(s, method))
                                            .forEach(s -> {
                                                log.info(aMarker, "Generating test case for method: {} in class: {}", method, className);
                                                String prompt = methodName.getQuestion().getPrompt();
                                                urls.forEach(url -> executorService.submit(() -> {
                                                    try {
                                                        doGenerateTestCase(documentType, projectType, packageName, method, prompt, jdbi, outputTable, tenantId, url, groupId, className);
                                                    } catch (Exception e) {
                                                        log.error(aMarker, "The Exception occurred in test case generation for class: {}, \n method: \n {}", className, method);
                                                        HandymanException handymanException = new HandymanException(e);
                                                        HandymanException.insertException("Exception occurred in test case generation for class - " + className + "\n method: \n" + method, handymanException, this.action);
                                                    } finally {
                                                        log.info("Consumer {} completed the process for class {}, \n method: \n {}", countDownLatch.getCount(), className, method);
                                                        countDownLatch.countDown();
                                                    }
                                                }));
                                            });
                                });
                            });
                });
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("Consumer Interrupted with exception", e);
                throw new HandymanException("Consumer Interrupted with exception", e, action);
            }
        } else {
            log.error(aMarker, "Endpoints for test case generation is empty");
        }
    }

    private @NotNull CountDownLatch getCountDownLatch(List<Channel> channels, List<JavaClassMap> generatingClasses, List<URL> urls) {
        AtomicInteger methodSize = new AtomicInteger();

        channels.stream()
                .flatMap(channel -> channel.getSips().stream())
                .forEach(sip ->
                        generatingClasses.stream()
                                .filter(generatingClass -> compareIgnoringWhitespace(sip.getClassName(), generatingClass.getClassName()))
                                .forEach(generatingClass ->
                                        sip.getSynonyms().stream()
                                                .filter(synonym -> generatingClass.getMethods().stream()
                                                        .anyMatch(method -> {
                                                            System.out.println(method);
                                                            System.out.println(synonym.getMethodName());
                                                            boolean matched = compareIgnoringWhitespace(synonym.getMethodName(), method);
                                                            return matched;
                                                        })
                                                )
                                                .forEach(synonym -> methodSize.incrementAndGet())
                                )
                );

        int endpointSize = urls.size();
        CountDownLatch countDownLatch = new CountDownLatch(methodSize.get() * endpointSize);
        log.info("Total methods: {}, Total endpoints: {}, and countDownLatch count {}", methodSize, endpointSize, countDownLatch.getCount());
        return countDownLatch;
    }

    public boolean compareIgnoringWhitespace(String firstValue, String secondValue) {
        return firstValue.replaceAll("\\s+", "").equalsIgnoreCase(secondValue.replaceAll("\\s+", ""));
    }


    private void doGenerateTestCase(String documentType, String projectType, String packageName, String method, String prompt, Jdbi jdbi, String outputTable, Long tenantId, URL url, Long groupId, String className) {

        Request request = new Request.Builder()
                .url(url + prompt)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .addHeader("accept", "application/json")
                .build();

        LocalDateTime requestedTime = LocalDateTime.now();

        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                LocalDateTime responseTime = LocalDateTime.now();
                insertResponseDetails(responseBody, documentType, projectType, packageName, method, prompt, jdbi, requestedTime, responseTime, outputTable, tenantId, groupId, className);
                log.info("Response: {}", responseBody);
            } else {
                LocalDateTime responseTime = LocalDateTime.now();
                insertResponseDetails(responseBody, documentType, projectType, packageName, method, prompt, jdbi, requestedTime, responseTime, outputTable, tenantId, groupId, className);
            }
        } catch (Throwable t) {
            log.error(aMarker, "error in api response for test case generation {}", t.getMessage());
            throw new HandymanException("error in api response for test case generation", t, action);
        }
    }

    private void insertResponseDetails(String responseBody, String documentType, String projectType, String packageName, String method, String prompt, Jdbi jdbi, LocalDateTime requestedTime, LocalDateTime responseTime, String outputTable, Long tenantId, Long groupId, String className) {
        long diffInSeconds = Duration.between(requestedTime, responseTime).getSeconds();
        String formattedTime = formatTime(diffInSeconds);
        Long rootPipelineId = action.getRootPipelineId();
        String sql = "INSERT INTO " + outputTable + " (document_type, project_type, package_name, class_name, method_name, prompt, prompt_response, requested_time, responded_time, execution_time, tenant_id, root_pipeline_id, group_id) " +
                "VALUES (:documentType, :projectType, :packageName, :className, :methodName, :prompt, :promptResponse, :requestedTime, :respondedTime, :executionTime, :tenantId, :rootPipelineId, :groupId)";
        jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind("documentType", documentType)
                .bind("projectType", projectType)
                .bind("packageName", packageName)
                .bind("className", className)
                .bind("methodName", method)
                .bind("prompt", prompt)
                .bind("promptResponse", responseBody)
                .bind("requestedTime", requestedTime)
                .bind("respondedTime", responseTime)
                .bind("executionTime", formattedTime)
                .bind("tenantId", tenantId)
                .bind("rootPipelineId", rootPipelineId)
                .bind("groupId", groupId)
                .execute());
    }

    public static String formatTime(double seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds must be a non-negative number.");
        }
        if (seconds < 60) {
            return String.format("%.2f sec", seconds);
        } else if (seconds < 3600) {
            int minutes = (int) (seconds / 60);
            double sec = seconds % 60;
            return String.format("%d min %.2f sec", minutes, sec);
        } else {
            int hours = (int) (seconds / 3600);
            int minutes = (int) ((seconds % 3600) / 60);
            double sec = seconds % 60;
            if (minutes == 0) {
                return String.format("%d hr %.2f sec", hours, sec);
            } else {
                return String.format("%d hr %d min %.2f sec", hours, minutes, sec);
            }
        }
    }

    private void getChannelDetails(Path projectDir, List<Channel> channels, Jdbi jdbi, Long tenantId, String questionVerseSchema) {
        try (Stream<Path> paths = Files.walk(projectDir)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        String relativePath = projectDir.relativize(path).toString();
                        String packageName = getPackageName(relativePath);
                        String className = getClassName(path);

                        Channel channel = channels.stream()
                                .filter(c -> c.packageName.equals(packageName))
                                .findFirst()
                                .orElseGet(() -> {
                                    Channel newChannel = new Channel();
                                    newChannel.packageName = packageName;
                                    channels.add(newChannel);
                                    return newChannel;
                                });

                        SIP sip = new SIP();
                        sip.className = className;
                        channel.sips.add(sip);

                        try {
                            List<String> lines = Files.readAllLines(path);
                            String content = String.join(System.lineSeparator(), lines);

                            JavaParser javaParser = new JavaParser();
                            FileInputStream in = new FileInputStream(path.toFile());

                            CompilationUnit compilationUnit = new CompilationUnit();
                            Optional<CompilationUnit> optionalCompilationUnit = javaParser.parse(in).getResult();
                            if (optionalCompilationUnit.isPresent()) {
                                compilationUnit = optionalCompilationUnit.get();
                            }
                            else {
                                log.error("Could not parse the file for reading method names");
                            }

                            // Create a MethodVisitor instance
                            MethodVisitor methodVisitor = new MethodVisitor();

                            // Visit methods
                            methodVisitor.visit(compilationUnit, null);

                            // Get the method signatures
                            List<String> methodNames = methodVisitor.getMethodSignatures();


                            for (String methodName : methodNames) {
                                Synonym synonym = new Synonym();
                                synonym.methodName = methodName;
                                synonym.question = new Question();


                                String fileIdQuery = "select ai.template_name, te.truth_entity_name, st.synonym, sq.question, sq.tenant_id " +
                                        "from " + questionVerseSchema + ".asset_info ai \n" +
                                        "join " + questionVerseSchema + ".truth_entity te on te.asset_id = ai.asset_id \n" +
                                        "join " + questionVerseSchema + ".sor_tsynonym st on st.truth_entity_id = te.truth_entity_id \n" +
                                        "join " + questionVerseSchema + ".sor_question sq on sq.synonym_id = st.synonym_id \n" +
                                        "where te.sip_type = 'CODE_SIP' and sq.tenant_id = " + tenantId + " and ai.template_name= '" + packageName + "' and te.truth_entity_name = '" + className + "' and st.synonym = '" + methodName + "'";

                                JavaTestCasePromptQueryResult javaTestCasePromptQueryResult;
                                try {
                                    javaTestCasePromptQueryResult = jdbi.withHandle(handle -> handle.createQuery(fileIdQuery).mapToBean(JavaTestCasePromptQueryResult.class).one());
                                } catch (RuntimeException e) {
                                    log.error(aMarker, "error getting prompt from questionVerse for package: {}, class: {}, method:{} with exception {}", packageName, className, methodName, e.getMessage());
                                    throw new HandymanException("error getting prompt from questionVerse for package:" + packageName + ",class:" + className + ",method:" + methodName, e, action);
                                }

                                String basePrompt = javaTestCasePromptQueryResult.getQuestion();
                                final Map<String, String> context = new HashMap<>();
                                context.put("className", className);
                                context.put("methodName", methodName);
                                context.put("code", content);

                                var paramEngine = new StringSubstitutor(context);
                                synonym.question.prompt = paramEngine.replace(basePrompt);
                                sip.synonyms.add(synonym);
                            }
                        } catch (IOException e) {
                            log.error("Could not read file: {} with exception {}", path, e.getMessage());
                            throw new HandymanException("Could not read file", e, action);
                        }
                    });
        } catch (IOException e) {
            log.error(aMarker, "error in reading files for test case generation ", e);
            throw new HandymanException("error in reading files for test case generation", e, action);
        }
    }

    private static List<String> extractMethodsWithParameters(List<String> lines) {
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
                if (parameter.isFinal()) {
                    methodSignature.append("final ");
                }
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


    private static String getPackageName(String relativePath) {
        String[] parts = relativePath.split(FileSystems.getDefault().getSeparator());
        return String.join(".", parts).substring(0, relativePath.lastIndexOf(FileSystems.getDefault().getSeparator())).replace(".java", "");
    }

    private static String getClassName(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    @Override
    public boolean executeIf() throws Exception {
        return javaTestCaseGenerator.getCondition();
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JavaTestCaseQueryResult {

        private String documentType;
        private String projectType;
        private String folderPath;
        private Long tenantId;
        private Long userId;
        private String classValues;
        private Long groupId;
        private String questionVerseSchema;
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


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JavaTestCasePromptQueryResult {
        private String templateName;
        private String truthEntityName;
        private String synonym;
        private String question;
        private Long tenantId;
    }


    @Data
    public static class Channel {
        String packageName;
        List<SIP> sips = new ArrayList<>();
    }

    @Data
    public static class SIP {
        String className;
        List<Synonym> synonyms = new ArrayList<>();
    }

    @Data
    public static class Synonym {
        String methodName;
        Question question;
    }

    @Data
    public static class Question {
        String prompt;
    }

}