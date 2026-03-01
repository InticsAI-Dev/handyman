package in.handyman.raven.lib.services.sor.transform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstanceFingerprint {
    private String instanceName;
    private Set<String> sectionAliases = new HashSet<>();
    private Set<String> answers = new HashSet<>();
    private List<MultiEntityFieldHandlingInput> items;
}