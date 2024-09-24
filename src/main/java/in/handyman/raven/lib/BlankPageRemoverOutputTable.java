package in.handyman.raven.lib;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//2. output pojo for table, which implements CoproProcessor.Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlankPageRemoverOutputTable implements CoproProcessor.Entity {

    private String originId;
    private Integer groupId;
    private String processedFilePath;
    private String status;
    private String stage;
    private String message;
    private String request;
    private String response;
    private String endpoint;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.processedFilePath, this.status, this.stage, this.message, this.request, this.response, this.endpoint).collect(Collectors.toList());
    }
}
