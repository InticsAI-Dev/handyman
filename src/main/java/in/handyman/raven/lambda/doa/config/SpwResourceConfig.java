package in.handyman.raven.lambda.doa.config;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import in.handyman.raven.lambda.doa.Auditable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import in.handyman.raven.core.dataaccess.DataAccessRegistry;
import org.jdbi.v3.core.Jdbi;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpwResourceConfig extends Auditable {

    private Long id;

    private String configName;
    private String driverClass;
    private String userName;
    private String password;
    private String resourceUrl;
    private String host;
    private String port;
    private String databaseName;

    private boolean active;
    private Integer version;

    public Jdbi get() {
        return DataAccessRegistry.get(configName).asJdbi();
    }

}
