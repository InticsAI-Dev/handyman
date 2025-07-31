package in.handyman.raven.lib.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AumiOutboundResponse {

    private String requestTxnId;
    private String status;
    private String errorMessage;
    private String errorMessageDetail;
    private Object errorCd;
    private String documentId;
    private OutboundJsonMetaData metadata;
    private AumiPayload aumipayload;
    private String customResponse;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OutboundJsonMetaData {
        private String documentType;
        private String documentExtension;
        private String transactionId;
        private String inboundDocumentName;
        private LocalDateTime processStartTime;
        private LocalDateTime processEndTime;
        private Long processingTimeMs;
        private LocalDateTime processedAt;
        private Integer pageCount;
        private List<Integer> candidatePaper;
        private Integer overallConfidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExtractedField {
        private String value;
        private Integer page;
        private Double confidence;
        private JsonNode boundingBox;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CategoryField {
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Provider {
        private CategoryField providerCategory;
        private ExtractedField providerNPI;
        private ExtractedField providerTIN;
        private ExtractedField providerFirstName;
        private ExtractedField providerLastName;
        private ExtractedField providerAddressLine1;
        private ExtractedField providerAddressLine2;
        private ExtractedField providerCity;
        private ExtractedField providerState;
        private ExtractedField providerZipCode;
        private ExtractedField providerSpeciality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceModifier {
        private ExtractedField cd;
        private ExtractedField desc;
        private ExtractedField lineNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Diagnosis {
        private ExtractedField cd;
        private ExtractedField desc;
        private ExtractedField codePointer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdditionalProperties {
        private String propName;
        private String propValue;
        private Integer page;
        private Double confidence;
        private JsonNode boundingBox;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuthorizationIndicator {
        private String propName;
        private String propValue;
        private Integer page;
        private Double confidence;
        private JsonNode boundingBox;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AumiPayload {
        private ExtractedField authId;
        @JsonProperty("member_id")
        private ExtractedField hcid;
        private ExtractedField medicaidId;
        private ExtractedField memberLastName;
        private ExtractedField memberFirstName;
        private ExtractedField memberDOB;
        private ExtractedField groupId;
        private ExtractedField memberGender;
        private ExtractedField memberAddressLine1;
        private ExtractedField memberCity;
        private ExtractedField memberZipCode;
        private ExtractedField memberState;
        private ExtractedField levelOfService;
        private ExtractedField serviceFromDate;
        private ExtractedField serviceToDate;
        private ExtractedField authAdmitDate;
        private ExtractedField authDischargeDate;
        private ExtractedField authDischargeDisposition;
        private List<Provider> providers;
        private List<ServiceModifier> serviceModifiers;
        private List<Diagnosis> diagnosis;
        private List<AdditionalProperties> additionalProperties;
        private List<AuthorizationIndicator> authorizationIndicators;
    }
}