package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.WordCountAdapter;
import in.handyman.raven.lib.interfaces.AdapterInterface;
import in.handyman.raven.lib.model.Wordcount;

import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "Wordcount"
)
public class WordcountAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final Wordcount wordcount;

    private final Marker aMarker;

    public WordcountAction(final ActionExecutionAudit action, final Logger log,
                           final Object wordcount) {
        this.wordcount = (Wordcount) wordcount;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" Wordcount:" + this.wordcount.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(aMarker, "<-------Word Count Action for {} has been started------->" + wordcount.getName());
            AdapterInterface wordCountAdapter = new WordCountAdapter();
            int wordCount = wordCountAdapter.getThresoldScore(wordcount.getInputValue());
            int confidenceScore = wordCount <= Integer.parseInt(wordcount.getCountLimit())
                    ? Integer.parseInt(wordcount.getThresholdValue()) : 0;
            action.getContext().put(wordcount.getName().concat(".score"), String.valueOf(confidenceScore));
            log.info(aMarker, "<-------Word Count Action for {} has been completed------->" + wordcount.getName());

        } catch (Exception ex) {
            action.getContext().put(wordcount.getName().concat(".error"), "true");
            log.info(aMarker, "The Exception occurred ", ex);
            throw new HandymanException("Failed to execute", ex);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return wordcount.getCondition();
    }
}
