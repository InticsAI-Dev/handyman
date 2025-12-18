package in.handyman.raven.lib.services.neradaptors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NerAdapterAuditResponse {

    private String endpoint;
    private String response;
    private String request;

}
