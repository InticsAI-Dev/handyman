package in.handyman.raven.lib.custom.outbound.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Provider {
    @JsonProperty("providerCategory")
    private CategoryField providerCategory;
    @JsonProperty("providerNPI")
    private ExtractedField providerNPI;
    @JsonProperty("providerTIN")
    private ExtractedField providerTIN;
    @JsonProperty("providerFirstName")
    private ExtractedField providerFirstName;
    @JsonProperty("providerLastName")
    private ExtractedField providerLastName;
    @JsonProperty("providerAddressLine1")
    private ExtractedField providerAddressLine1;
    @JsonProperty("providerAddressLine2")
    private ExtractedField providerAddressLine2;
    @JsonProperty("providerCity")
    private ExtractedField providerCity;
    @JsonProperty("providerState")
    private ExtractedField providerState;
    @JsonProperty("providerZipCode")
    private ExtractedField providerZipCode;
    @JsonProperty("providerSpeciality")
    private ExtractedField providerSpeciality;

}