package in.handyman.raven.exception;

/**
 * Thrown when a copro action enters WAITING_FOR_ASYNC state.
 * The pipeline execution loop catches this to halt gracefully
 * without marking the pipeline as FAILED.
 * The continuation script will be triggered later by Vulcan(PipelineAggregatorService).
 */
public class PipelineHaltException extends RuntimeException {

    public PipelineHaltException(String message) {
        super(message);
    }

    public PipelineHaltException(String message, Throwable cause) {
        super(message, cause);
    }
}
