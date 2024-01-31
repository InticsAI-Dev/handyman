package in.handyman.raven.lib.model.tableextraction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.postgresql.util.PGobject;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableHeader {
    @JsonProperty("TruthEntity")
    private String truthEntity;
    private List<String> columnHeaders;
}
