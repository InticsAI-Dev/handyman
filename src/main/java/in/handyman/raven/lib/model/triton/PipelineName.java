package in.handyman.raven.lib.model.triton;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PipelineName {

    PAPER_ITEMIZER("PAPER_ITEMIZER"),AUTO_ROTATION("AUTO_ROTATION"),TEXT_EXTRACTION("TEXT_EXTRACTION");
    private final String processName;
}
