package in.handyman.raven.lib.model.triton;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PipelineName {

    PAPER_ITEMIZER("PAPER_ITEMIZER"),
    AUTO_ROTATION("AUTO_ROTATION"),
    TEXT_EXTRACTION("TEXT_EXTRACTION"),
    ZERO_SHOT_CLASSIFIER("ZERO_SHOT_CLASSIFIER"),
    PHRASE_MATCH("PHRASE_MATCH"),
    PAPER_CLASSIFICATION("PAPER_CLASSIFICATION"), 
    QR_EXTRACTION("QR_EXTRACTION"),
    URGENCY_TRIAGE("URGENCY_TRIAGE"),
    VQA_TRANSACTION("VQA_TRANSACTION"),
    CONTROL_DATA("CONTROL_DATA_COS"),
    FILE_MERGER("FILE_MERGER"),
    NOISE_DETECTION("NOISE_DETECTION")
    ;
    
    private final String processName;
}
