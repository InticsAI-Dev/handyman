package in.handyman.raven.lib.services.headers.copro.legacy.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableData {
    public List<String> columnHeaders;
    public List<List<String>> data;
}
