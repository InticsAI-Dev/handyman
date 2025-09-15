package in.handyman.raven.lambda.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.actor.HandymanActorSystemAccess;
import in.handyman.raven.compiler.RavenParser;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ConfigAccess;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.access.repo.HandymanRepoImpl;
import in.handyman.raven.lambda.action.IActionContext;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.AbstractAudit;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.ExecutionGroup;
import in.handyman.raven.lambda.doa.audit.ExecutionStatus;
import in.handyman.raven.lambda.doa.audit.PipelineExecutionAudit;
import in.handyman.raven.util.UniqueID;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.SubstituteLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class LambdaEngine {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HandymanRepo REPO = new HandymanRepoImpl();
    private static final String RAVEN_VM = "RavenVM";
    private static final String THROW_EXCEPTION = "throw_exception";

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    private LambdaEngine() {
    }

    /**
     * Execution starts from here
     */
    public static PipelineExecutionAudit start(final LContext lContext) throws HandymanException {

//        log.info("the given request  => " + lContext);
        final String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostAddress();
            log.info("Execution Engine Starts in host {}", hostName);
        } catch (UnknownHostException e) {
            throw new HandymanException("hostName not found ", e);
        }
        final PipelineExecutionAudit pipelineExecutionAudit = PipelineExecutionAudit.builder()
                .hostName(hostName)
                .modeOfExecution(RAVEN_VM)
                .threadName(Thread.currentThread().getName())
                .build();
        pipelineExecutionAudit.setProcessId(ProcessHandle.current().pid());
        if (lContext.getRootPipelineId() == null) {
            final Long id = UniqueID.getId();
            pipelineExecutionAudit.setRootPipelineId(id);
            pipelineExecutionAudit.setPipelineId(id);
            HandymanActorSystemAccess.insert(pipelineExecutionAudit);
            lContext.setRootPipelineId(pipelineExecutionAudit.getRootPipelineId());
        } else {
            pipelineExecutionAudit.setRootPipelineId(lContext.getRootPipelineId());
            HandymanActorSystemAccess.insert(pipelineExecutionAudit);
        }
        log.info("LContext rootID => {} ", lContext.getRootPipelineId());
        pipelineExecutionAudit.updateExecutionStatusId(ExecutionStatus.STAGED.getId());
        log.info("Started building the pipeline context");
        try {
            pipelineExecutionAudit.updateExecutionStatusId(ExecutionStatus.STARTED.getId());
            final Map<String, String> context = getEContext(lContext.getPipelineName());
            log.info("Raven Context has been initialized");
            final String processFile = getProcessFile(lContext, context);
            context.put("parent-pipeline-id", String.valueOf(lContext.getParentPipelineId()));
            context.put("pipeline-id", String.valueOf(pipelineExecutionAudit.getPipelineId()));
            context.put("process-id", String.valueOf(pipelineExecutionAudit.getPipelineId()));
            context.put("root-ref-id", String.valueOf(pipelineExecutionAudit.getPipelineId()));
            context.putAll(lContext.getInheritedContext());
//            final Map<String, String> other = getOther(pipeline);
//            context.putAll(other);
            log.info("Raven context has been populated with inheritedContext");
            final RavenParserContext ravenParserContext = getRavenParserContext(processFile, lContext.getLambdaName(), context);

            toPipeline(lContext, pipelineExecutionAudit);

            final String relativePath = context.get(HRequestResolver.LoadType.FILE.getVariable());
//            if (relativePath != null) {
//                final int i = relativePath.indexOf(pipeline.getPipelineName());
//                pipeline.setLambdaName(relativePath.substring(i ).replace(pipeline.getPipelineName() + "/", ""));
//            }
            pipelineExecutionAudit.setProcessName(ravenParserContext.getProcessName());
            pipelineExecutionAudit.setContext(context);

            log.info("Initial Pipeline context has been build successfully");
            try {
                log.info("Pipeline execution has been started");
                pipelineExecutionAudit.updateExecutionStatusId(ExecutionStatus.RUNNING.getId());
                run(pipelineExecutionAudit, ravenParserContext.getTryContext(), context, ExecutionGroup.TRY);
                log.info("Pipeline execution has been completed successfully");
            } catch (Exception e) {
                log.info("Started Executing the catch block");
                run(pipelineExecutionAudit, ravenParserContext.getCatchContext(), context, ExecutionGroup.CATCH);
                pipelineExecutionAudit.updateExecutionStatusId(ExecutionStatus.FAILED.getId());
                log.error("try section failed", e);
                log.error("Completed Execution catch block");
                if (!Objects.equals("false", context.get(THROW_EXCEPTION))) {
                    throw new HandymanException("Failed ", e);
                }
            } finally {
                log.info("Executing Finally Block");
                run(pipelineExecutionAudit, ravenParserContext.getFinallyContext(), context, ExecutionGroup.FINALLY);
                HandymanActorSystemAccess.update(pipelineExecutionAudit);
                log.info("Completed execution finally block");
                if (ExecutionStatus.get(pipelineExecutionAudit.getExecutionStatusId()) == ExecutionStatus.RUNNING) {
                    final List<ActionExecutionAudit> actionExecutionAudits = REPO.findActions(pipelineExecutionAudit.getPipelineId());
                    final Integer executionStatusId = actionExecutionAudits.stream().filter(action -> ExecutionStatus.get(pipelineExecutionAudit.getExecutionStatusId()) != ExecutionStatus.COMPLETED)
                            .findFirst().map(ActionExecutionAudit::getExecutionStatusId).orElse(ExecutionStatus.COMPLETED.getId());
                    pipelineExecutionAudit.updateExecutionStatusId(executionStatusId);
                }
                HandymanActorSystemAccess.update(pipelineExecutionAudit);
            }
        } catch (Exception e) {
            log.error("Process section failed", e);
            pipelineExecutionAudit.updateExecutionStatusId(ExecutionStatus.FAILED.getId());
            HandymanActorSystemAccess.update(pipelineExecutionAudit);
            throw new HandymanException("Failed for " + pipelineExecutionAudit.getPipelineName(), e);
        }
        return pipelineExecutionAudit;
    }

    private static HashMap<String, String> getEContext(final String lambdaName) {
        log.info("Configuration for the lambda {}", lambdaName);
        return new HashMap<>(ConfigAccess.getAllConfig(lambdaName));
    }

    private static String getProcessFile(final LContext lContext, final Map<String, String> context) {
        final String lambdaName = lContext.getLambdaName();
        final String relativePath = lContext.getRelativePath();
        log.info("Started initializing raven context ");
        final String processLoadType = lContext.getProcessLoadType();
        final String processFile;
        if (lContext.getParentPipelineId() != null) {
            processFile = getProcessFile(HRequestResolver.LoadType.FILE.name(), lambdaName, context, relativePath);
        } else if (HRequestResolver.LoadType.valueOf(processLoadType) == HRequestResolver.LoadType.REST) {
            processFile = lContext.getPayload();
        } else {
            processFile = getProcessFile(processLoadType, lambdaName, context, null);
        }
        return processFile;
    }

    private static RavenParserContext getRavenParserContext(final String processFile, final String lambdaName,
                                                            final Map<String, String> context) {
        log.debug("Handyman Engine start for {}", lambdaName);
        final RavenParser.ProcessContext ravenParser = LambdaParser.doParse(processFile);
        return RavenParserContext.builder()
                .processName(CommandProxy.getString(ravenParser.name, context))
                .tryContext(ravenParser.tryBlock.actions)
                .catchContext(ravenParser.catchBlock.actions)
                .finallyContext(ravenParser.finallyBlock.actions)
                .build();
    }

    private static void toPipeline(final LContext lContext, final PipelineExecutionAudit pipelineExecutionAudit) {
        pipelineExecutionAudit.setRelativePath(lContext.getRelativePath());
        pipelineExecutionAudit.setPipelineLoadType(lContext.getProcessLoadType());
        pipelineExecutionAudit.setLambdaName(lContext.getLambdaName());
        pipelineExecutionAudit.setPipelineName(lContext.getPipelineName());
        pipelineExecutionAudit.setParentPipelineId(lContext.getParentPipelineId());
        pipelineExecutionAudit.setParentPipelineName(lContext.getParentPipelineName());
        pipelineExecutionAudit.setParentActionId(lContext.getParentActionId());
        pipelineExecutionAudit.setParentActionName(lContext.getParentActionName());
        pipelineExecutionAudit.setRequestBody(lContext.getPayload());
        pipelineExecutionAudit.setCreatedBy(lContext.getUserId());
    }

    private static void run(final PipelineExecutionAudit pipelineExecutionAudit,
                            final List<RavenParser.ActionContext> contexts,
                            final Map<String, String> context,
                            final ExecutionGroup executionGroup) {
        contexts.forEach(actionContext -> {
            var action = ActionExecutionAudit.builder()
                    .executionGroupId(executionGroup.getId())
                    .pipelineId(pipelineExecutionAudit.getPipelineId())
                    .build();
            action.setContext(context);
            action.setPipelineId(pipelineExecutionAudit.getPipelineId());
            toAction(action, pipelineExecutionAudit);


            doAction(action, actionContext);


        });
    }

    private static String getProcessFile(final String processLoadType, final String lambdaName, final Map<String, String> context, final String relativePath) {
        final String processFile = relativePath != null ? HRequestResolver.readFile(relativePath, "") :
                HRequestResolver.doResolve(lambdaName, processLoadType, context);
        if (Objects.isNull(processFile)) {
            throw new HandymanException("Content configuration for process " + lambdaName + " is missing, check spw_process_config or spw_instance_config");
        }
        if (processFile.isEmpty()) {
            throw new HandymanException("Content configuration for process " + lambdaName + " is missing, check spw_process_config or spw_instance_config");
        }
        return processFile;
    }

    public static void toAction(final ActionExecutionAudit actionExecutionAudit, final AbstractAudit abstractAudit) {
        actionExecutionAudit.setThreadName(Thread.currentThread().getName());
        actionExecutionAudit.setProcessId(ProcessHandle.current().pid());
        actionExecutionAudit.setCreatedBy(abstractAudit.getCreatedBy());
        actionExecutionAudit.setPipelineName(abstractAudit.getPipelineName());
        actionExecutionAudit.setLambdaName(abstractAudit.getLambdaName());
        actionExecutionAudit.setParentActionId(abstractAudit.getParentActionId());
        actionExecutionAudit.setParentActionName(abstractAudit.getParentActionName());
        actionExecutionAudit.setParentPipelineId(abstractAudit.getParentPipelineId());
        actionExecutionAudit.setParentPipelineName(abstractAudit.getParentPipelineName());
        actionExecutionAudit.setRootPipelineId(abstractAudit.getRootPipelineId());
    }

    public static void doAction(final ActionExecutionAudit actionExecutionAudit, final RavenParser.ActionContext actionContext) {
        final SubstituteLogger logger = getLogger(actionExecutionAudit);
        HandymanActorSystemAccess.insert(actionExecutionAudit);
        actionExecutionAudit.updateExecutionStatusId(ExecutionStatus.STAGED.getId());
        HandymanActorSystemAccess.update(actionExecutionAudit);
        logger.info("\n");
        String actionName = actionExecutionAudit.getActionName();
        Long actionId = actionExecutionAudit.getActionId();
        try {
            logger.info("Loading context for executing action with id {}", actionId);
            final IActionExecution execution = load(actionContext, actionExecutionAudit);
            actionName=actionExecutionAudit.getActionName();
            logger.info("Action execution has been started for action name {} with id {}", actionName, actionId);
            execute(execution, actionExecutionAudit);
            logger.info("Execution has been completed successfully action name {} with id {}", actionName, actionId);
        } catch (Exception e) {
            logger.error("Error executing action " + actionName, e);
            actionExecutionAudit.updateExecutionStatusId(ExecutionStatus.FAILED.getId());
            throw new HandymanException("Error executing action "+ actionName, e);
        } finally {
            HandymanActorSystemAccess.update(actionExecutionAudit);
            final StringBuilder stringBuilder = new StringBuilder();
            logger.info("Started collecting Lambda engine logs {}", actionName);
            stringBuilder.append("\n");
            actionExecutionAudit.getEventQueue().forEach(event -> {
                if(event!=null) {
                    ZonedDateTime localTime = Instant.ofEpochMilli(event.getTimeStamp())
                            .atZone(ZoneId.systemDefault());

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                    append(stringBuilder, formatter.format(localTime));
                    stringBuilder.append(" ");
                    append(stringBuilder, event.getThreadName());
                    stringBuilder.append(" ");
                    append(stringBuilder, event.getLevel());
                    stringBuilder.append(" ");
//                final String markersName = Optional.ofNullable(event.getMarker()).map(markers -> markers.stream().map(Marker::getName).collect(Collectors.joining(","))).orElse("");
//                append(stringBuilder, markersName);
//                stringBuilder.append(" ");
                    append(stringBuilder, MessageFormatter.arrayFormat(event.getMessage(), event.getArgumentArray()).getMessage());
                    if (event.getThrowable() != null) {
                        var sw = new StringWriter();
                        var pw = new PrintWriter(sw);
                        event.getThrowable().printStackTrace(pw);
                        var sStackTrace = sw.toString();
                        stringBuilder.append("\n");
                        stringBuilder.append(sStackTrace);
                    }
                    stringBuilder.append("\n");
                }
            });
            logger.info("\n");
            log.info("Completed collecting LambdaEngine logs for action "+actionName);
            actionExecutionAudit.setLog(stringBuilder.toString());
            log.info(stringBuilder.toString());
            HandymanActorSystemAccess.update(actionExecutionAudit);
        }
    }

    public static SubstituteLogger getLogger(final ActionExecutionAudit actionExecutionAudit) {
        return new SubstituteLogger(actionExecutionAudit.getActionName(), actionExecutionAudit.getEventQueue(), false);
    }

    private static IActionExecution load(final RavenParser.ActionContext actionContext, final ActionExecutionAudit actionExecutionAudit) {
        final ParseTree child = actionContext.getChild(0);
        if (child != null) {
            final String macroName = child.getClass().getSimpleName().replace(ProcessExecutor.CONTEXT, "");
            actionExecutionAudit.setMacroName(macroName);
            final Logger logger = getLogger(actionExecutionAudit);
            logger.info("Execution class {} staged", macroName);
            if (ProcessExecutor.ACTION_CONTEXT_MAP.containsKey(macroName) && ProcessExecutor.ACTION_EXECUTION_MAP.containsKey(macroName)) {
                try {
                    actionExecutionAudit.updateExecutionStatusId(ExecutionStatus.STARTED.getId());
                    return initialization(child, actionExecutionAudit);
                } catch (Exception e) {
                    logger.trace("Error at Initialization " + macroName, e);
                }
            } else {
                logger.trace("Error Not found action " + macroName);
            }
        }
        throw new HandymanException("Unknown ActionContext");
    }

    public static void execute(final IActionExecution execution, final ActionExecutionAudit actionExecutionAudit) {
        final Logger logger = getLogger(actionExecutionAudit);
        final String actionName = actionExecutionAudit.getActionName();
        try {
            actionExecutionAudit.setThreadName(Thread.currentThread().getName());
            actionExecutionAudit.updateExecutionStatusId(ExecutionStatus.RUNNING.getId());
            logger.info("Execution class {} loaded", actionName);
            if (execution.executeIf()) {
                logger.info("Execution class {} condition passed", actionName);
                execution.execute();
                logger.info("Execution class {} executed", actionName);
            } else {
                logger.info("Execution class {} condition failed", actionName);
            }
            actionExecutionAudit.updateExecutionStatusId(ExecutionStatus.COMPLETED.getId());
        } catch (Exception e) {
            logger.trace("Error at Execution " + actionName, e);
            actionExecutionAudit.updateExecutionStatusId(ExecutionStatus.FAILED.getId());
            throw new HandymanException("Execution failed for " + actionName, e);
        }
    }

    private static void append(final StringBuilder stringBuilder, final Object value) {
        if (value != null) {
            stringBuilder.append(String.format("%s", value));
        }
    }

    //TODO preload actions for staging
    private static IActionExecution initialization(
            final ParseTree child,
            final ActionExecutionAudit actionExecutionAudit)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Logger logger = getLogger(actionExecutionAudit);
        final String macroName = actionExecutionAudit.getMacroName();

        final IActionContext actionContext = (IActionContext) ProcessExecutor.ACTION_CONTEXT_MAP.get(macroName).getConstructor().newInstance();
        logger.debug("actionContext Execution class {} instance created", macroName);
        CommandProxy.setTarget(actionContext, child, actionExecutionAudit.getContext());
        actionExecutionAudit.setActionName(actionExecutionAudit.getMacroName()+" -> "+actionContext.getName());
        if (logger.isDebugEnabled()) {
            logger.debug("actionContext Execution context {}", actionContext.getName());
//            logger.debug("actionContext Execution context {}", actionContext);
        }
        actionExecutionAudit.setInput(MAPPER.convertValue(actionContext, JsonNode.class));
        HandymanActorSystemAccess.update(actionExecutionAudit);

        return (IActionExecution) ProcessExecutor.ACTION_EXECUTION_MAP.get(macroName)
                .getConstructor(ActionExecutionAudit.class, Logger.class, Object.class)
                .newInstance(actionExecutionAudit, logger, actionContext);
    }

    public static ActionExecutionAudit getAction(final String actionName, final ActionExecutionAudit actionExecutionAudit) {
        var vAction = ActionExecutionAudit.builder()
                .pipelineId(actionExecutionAudit.getPipelineId())
                .executionGroupId(ExecutionGroup.ACTION.getId())
                .actionName(actionName)
                .build();
        vAction.setContext(actionExecutionAudit.getContext());
        LambdaEngine.toAction(vAction, actionExecutionAudit);
        vAction.setParentPipelineId(actionExecutionAudit.getPipelineId());
        vAction.setParentPipelineName(actionExecutionAudit.getPipelineName());
        vAction.setParentActionId(actionExecutionAudit.getActionId());
        vAction.setParentActionName(actionExecutionAudit.getActionName());
        return vAction;
    }

}
