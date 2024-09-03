package in.handyman.raven.lib.prompt.generation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceholderInput {
    private String placeholderKey;
    private String placeholderQuery;
}
