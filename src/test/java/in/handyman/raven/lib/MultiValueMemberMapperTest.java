package in.handyman.raven.lib;

import in.handyman.raven.lib.MultiValueMemberMapperAction.MultiValueMemberQueryInputTable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MultiValueMemberMapperTest {

    @Test
    void testEvaluateThresholdPerSorItem() {
        Logger mockLogger = Mockito.mock(Logger.class);
        Marker mockMarker = Mockito.mock(Marker.class);

        // Create sample input rows
        List<MultiValueMemberQueryInputTable> inputRows = new ArrayList<>();

        inputRows.add(MultiValueMemberQueryInputTable.builder()
                .sorItemName("member_id")
                .predictedValue("878142617, 87814267")
                .build());

        inputRows.add(MultiValueMemberQueryInputTable.builder()
                .sorItemName("member_last_name")
                .predictedValue("harbison natalya, natalya")
                .build());

        Set<String> targetSorItems = new HashSet<>(Arrays.asList("member_id","member_last_name"));

        String result = MultiValueMemberMapperAction.evaluateMultivaluePresenceAndUniqueness(
                inputRows, targetSorItems, mockLogger, mockMarker
        );

        System.out.println(result);
    }
}

