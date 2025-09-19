package in.handyman.raven.lib.model.agentic.paper.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AgenticPaperFilterAction;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionRequest;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionResponse;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpLineItem;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.TritonDataTypes;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_AGENTIC_FILTER_OUTPUT;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;
import static in.handyman.raven.exception.HandymanException.handymanRepo;

public class AgenticPaperFilterConsumerProcess implements CoproProcessor.ConsumerProcess<AgenticPaperFilterInput, AgenticPaperFilterOutput> {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final Marker aMarker;

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String AGENTIC_PAPER_FILTER_MODEL_NAME = "preprocess.agentic.paper.filter.model.name";
    private final AgenticPaperFilterCoproKryptonClient kryptonApiHandler;
    private CoproRetryService coproRetryService;
    public AgenticPaperFilterConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action,
                                             AgenticPaperFilterAction aAction, Integer pageContentMinLength,
                                             FileProcessingUtils fileProcessingUtils, String processBase64) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        int timeOut = aAction.getTimeOut();
        OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(timeOut, TimeUnit.MINUTES)
                .writeTimeout(timeOut, TimeUnit.MINUTES)
                .readTimeout(timeOut, TimeUnit.MINUTES)
                .build();

        coproRetryService = new CoproRetryService(handymanRepo, httpclient);
        this.kryptonApiHandler = new AgenticPaperFilterCoproKryptonClient(action, log, aMarker, pageContentMinLength, httpclient, processBase64, fileProcessingUtils, coproRetryService);
    }

    @Override
    public List<AgenticPaperFilterOutput> process(URL endpoint, AgenticPaperFilterInput entity) throws IOException {
        List<AgenticPaperFilterOutput> parentObj = new ArrayList<>();

        try {
            String textExtractionModelName = action.getContext().get(AGENTIC_PAPER_FILTER_MODEL_NAME);
            String filePath = entity.getFilePath();

            if (log.isInfoEnabled()) {
                log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} ", endpoint, filePath);
            }
            kryptonApiHandler.getCoproHandlerMethod(endpoint, entity, parentObj, textExtractionModelName);
        } catch (Exception e) {
            String errorMessage = "Error in process method for batch/group" + entity.getGroupId() + " originId " + entity.getOriginId() + " paperNo " + entity.getPaperNo() + "\n message: " + e.getMessage();
            log.error(aMarker, errorMessage, e);
            HandymanException.insertException(errorMessage, new HandymanException(e), this.action);
        }
        return parentObj;
    }
}