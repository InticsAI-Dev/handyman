package in.handyman.raven.lib.model.triton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrinityTritonResponse {
    private String filePath;
    private String responseBody;
    private String request;
    private String endpoint;
}
