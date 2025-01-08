package in.handyman.raven.lib.ganda;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
@ToString
public class GrievanceAppeals {
    private String requestTxnId;
    private String transactionId;
    private String status;
    private Metadata metadata;
    private List<PageInfo> pageInfo;

    // Getters and Setters
}