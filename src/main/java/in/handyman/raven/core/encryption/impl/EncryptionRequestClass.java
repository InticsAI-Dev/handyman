package in.handyman.raven.core.encryption.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EncryptionRequestClass {
    private String policy;
    private String value;
    private String key;
}
