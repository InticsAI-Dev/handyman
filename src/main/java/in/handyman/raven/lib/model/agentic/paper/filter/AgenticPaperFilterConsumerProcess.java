package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AgenticPaperFilterAction;
import in.handyman.raven.lib.CoproProcessor;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AgenticPaperFilterConsumerProcess implements CoproProcessor.ConsumerProcess<AgenticPaperFilterInput, AgenticPaperFilterOutput> {
    private final ActionExecutionAudit action;
    private final Logger log;
    private final Marker aMarker;

    public static final String AGENTIC_PAPER_FILTER_MODEL_NAME = "preprocess.agentic.paper.filter.model.name";
    private final AgenticPaperFilterCoproKryptonClient kryptonApiHandler;

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

        this.kryptonApiHandler = new AgenticPaperFilterCoproKryptonClient(action, log, aMarker, pageContentMinLength,
                httpclient, processBase64, fileProcessingUtils);
    }

    @Override
    public List<AgenticPaperFilterOutput> process(URL endpoint, AgenticPaperFilterInput entity) throws IOException {
        List<AgenticPaperFilterOutput> parentObj = new ArrayList<>();

        try {
            String textExtractionModelName = action.getContext().get(AGENTIC_PAPER_FILTER_MODEL_NAME);

            if (log.isInfoEnabled()) {
                log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} ", endpoint, entity.getFilePath());
            }

            kryptonApiHandler.getCoproHandlerMethod(endpoint, entity, parentObj, textExtractionModelName);
        } catch (Exception e) {
            String errorMessage = "Error in process method for batch/group" + entity.getGroupId() +
                    " originId " + entity.getOriginId() + " paperNo " + entity.getPaperNo() + "\n message: " + e.getMessage();
            log.error(aMarker, errorMessage, e);
            HandymanException.insertException(errorMessage, new HandymanException(e), this.action);
        }
        return parentObj;
    }
}