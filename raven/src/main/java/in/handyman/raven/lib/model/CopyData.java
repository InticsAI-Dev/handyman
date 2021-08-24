package in.handyman.raven.lib.model;

import in.handyman.raven.lambda.Lambda;
import in.handyman.raven.lambda.LambdaContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Auto Generated By Raven
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@LambdaContext(
        lambdaName = "CopyData"
)
public class CopyData implements Lambda {
    private String name;

    private String source;

    private String to;

    private String value;

    private Boolean condition = true;

    private String writeThreadCount;

    private String fetchBatchSize;

    private String writeBatchSize;
}
