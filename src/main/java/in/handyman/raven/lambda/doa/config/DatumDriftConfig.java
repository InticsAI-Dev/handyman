package in.handyman.raven.lambda.doa.config;

import in.handyman.raven.lambda.doa.Auditable;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatumDriftConfig extends Auditable {

    private long id;
    private String schemaName;
    private String tableName;
    private String tableType;
    private String cronExecutionTime;
    private boolean active;
    private int version;
}
