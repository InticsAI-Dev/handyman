package in.handyman.raven.lib.replicate;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ReplicateRequest {

//    private String version;
    private Object input;
}
