package in.handyman.raven.lib.custom.outbound.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AumiPayload {
    @JsonProperty("authId")
    private ExtractedField authId;
    @JsonProperty("hcid")
    private ExtractedField hcid;
    @JsonProperty("medicaidId")
    private ExtractedField medicaidId;
    @JsonProperty("memberLastName")
    private ExtractedField memberLastName;
    @JsonProperty("memberFirstName")
    private ExtractedField memberFirstName;
    @JsonProperty("memberDOB")
    private  ExtractedField memberDOB;
    @JsonProperty("groupId")
    private ExtractedField groupId;
    @JsonProperty("memberGender")
    private  ExtractedField memberGender;
    @JsonProperty("memberAddressLine1")
    private  ExtractedField memberAddressLine1;
    @JsonProperty("memberCity")
    private  ExtractedField memberCity;
    @JsonProperty("memberZipCode")
    private  ExtractedField memberZipCode;
    @JsonProperty("memberState")
    private  ExtractedField memberState;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("additionalProperties")
    private List<AdditionalProperties> additionalProperties;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("authorizationIndicators")
    private List<AuthorizationIndicator> authorizationIndicators;
    @JsonProperty("levelOfService")
    private ExtractedField levelOfService;
    @JsonProperty("serviceFromDate")
    private ExtractedField serviceFromDate;
    @JsonProperty("serviceToDate")
    private ExtractedField serviceToDate;
    @JsonProperty("service")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ServiceModifier> service;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("diagnosis")
    private List<Diagonsis> diagnosis;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("lengthOfStay")
    private List<LengthOfStay> lengthOfStay;
    @JsonProperty("provider")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Provider> provider;
    @JsonProperty("authAdmitDate")
    private ExtractedField authAdmitDate;
    @JsonProperty("authDischargeDate")
    private ExtractedField authDischargeDate;
    @JsonProperty("authDischargeDisposition")
    private ExtractedField authDischargeDisposition;

}
