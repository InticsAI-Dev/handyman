package in.handyman.raven.lib.model.fileMergerPdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TritonInputRequest {

    private List<Object> inputs;
}
