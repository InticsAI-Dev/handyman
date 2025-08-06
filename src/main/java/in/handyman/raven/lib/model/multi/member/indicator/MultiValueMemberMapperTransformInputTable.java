package in.handyman.raven.lib.model.multi.member.indicator;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MultiValueMemberMapperTransformInputTable implements CoproProcessor.Entity {
    private String originId;
    private List<extractedSorItemList> sorItemList;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.sorItemList).collect(Collectors.toList());
    }

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }


}
