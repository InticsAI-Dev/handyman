package in.handyman.raven.lib;

import in.handyman.raven.lib.services.sor.transform.MultiEntityFieldHandlingInput;
import in.handyman.raven.lib.services.sor.transform.MultiEntityFieldHandlingProcessor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiEntityFieldHandlingProcessorTest {

    private final MultiEntityFieldHandlingProcessor processor = new MultiEntityFieldHandlingProcessor();

    @Test
    void testSplittingMultiValue() {
        // Input: One multi_value item "A, B"
        MultiEntityFieldHandlingInput input = new MultiEntityFieldHandlingInput();
        input.setOriginId("origin1");
        input.setSorContainerInstance("instance1");
        input.setSorItemName("item1");
        input.setLineItemType("multi_value");
        input.setAnswer("A, B");
        input.setTransactionId("old-id");

        List<MultiEntityFieldHandlingInput> inputs = Collections.singletonList(input);
        List<MultiEntityFieldHandlingInput> result = processor.process(inputs);
        System.out.println("Test case for testSplittingMultiValue:");
        for(MultiEntityFieldHandlingInput item : result){

            System.out.println("originId: " + item.getOriginId() + ", sorContainerInstance: " + item.getSorContainerInstance() +
                    ", sorItemName: " + item.getSorItemName() + ", lineItemType: " + item.getLineItemType() +
                    ", answer: " + item.getAnswer() + ", transactionId: " + item.getTransactionId());
        }


        assertEquals(2, result.size());

        // result should contain A and B with new transaction IDs
        assertTrue(result.stream().anyMatch(i -> "A".equals(i.getAnswer()) && !i.getTransactionId().equals("old-id")));
        assertTrue(result.stream().anyMatch(i -> "B".equals(i.getAnswer()) && !i.getTransactionId().equals("old-id")));
    }

    @Test
    void testDeduplicationWithinOrigin() {
        // Input:
        // 1. multi_value "A, B"
        // 2. single_value "A"
        // All in same origin/instance

        MultiEntityFieldHandlingInput input1 = new MultiEntityFieldHandlingInput();
        input1.setOriginId("origin1");
        input1.setSorContainerInstance("instance1");
        input1.setSorItemName("item1");
        input1.setLineItemType("multi_value");
        input1.setAnswer("A, B");

        MultiEntityFieldHandlingInput input2 = new MultiEntityFieldHandlingInput();
        input2.setOriginId("origin1");
        input2.setSorContainerInstance("instance1");
        input2.setSorItemName("item1");
        input2.setLineItemType("single_value");
        input2.setAnswer("A");

        List<MultiEntityFieldHandlingInput> result = processor.process(Arrays.asList(input1, input2));
        System.out.println("Test case for testDeduplicationWithinOrigin:");
        for(MultiEntityFieldHandlingInput item : result){
            System.out.println("originId: " + item.getOriginId() + ", sorContainerInstance: " + item.getSorContainerInstance() +
                    ", sorItemName: " + item.getSorItemName() + ", lineItemType: " + item.getLineItemType() +
                    ", answer: " + item.getAnswer() + ", transactionId: " + item.getTransactionId());
        }

        // Expectation: "A" from input1, "B" from input1. input2 "A" is duplicate.
        // Size = 2
        assertEquals(2, result.size());
        long countA = result.stream().filter(i -> "A".equals(i.getAnswer())).count();
        assertEquals(1, countA);
    }

    @Test
    void testDifferentOriginsNoDeduplication() {
        MultiEntityFieldHandlingInput input1 = new MultiEntityFieldHandlingInput();
        input1.setOriginId("origin1");
        input1.setSorContainerInstance("instance1");
        input1.setSorItemName("item1");
        input1.setLineItemType("multi_value");
        input1.setAnswer("A, B");

        MultiEntityFieldHandlingInput input2 = new MultiEntityFieldHandlingInput();
        input2.setOriginId("origin2");
        input2.setSorContainerInstance("instance1");
        input2.setSorItemName("item1");
        input2.setLineItemType("single_value");
        input2.setAnswer("A");

        List<MultiEntityFieldHandlingInput> result = processor.process(Arrays.asList(input1, input2));
        System.out.println("Test case for testDifferentOriginsNoDeduplication:");
        for(MultiEntityFieldHandlingInput item : result){
            System.out.println("originId: " + item.getOriginId() + ", sorContainerInstance: " + item.getSorContainerInstance() +
                    ", sorItemName: " + item.getSorItemName() + ", lineItemType: " + item.getLineItemType() +
                    ", answer: " + item.getAnswer() + ", transactionId: " + item.getTransactionId());
        }
        // Origin1: A, B
        // Origin2: A
        // Total 3
        assertEquals(3, result.size());
    }
}
