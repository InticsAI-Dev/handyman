package in.handyman.raven.lib.ganda;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class Metadata {
    private String documentId;
    private String processedAt;
    private int pageCount;
    private String documentType;
    private String processingTimeMs;
    private String overallConfidence;
}
