package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.structured.Scopes;

public class FlowSplitTest {

    // region split tests

    @Test
    void shouldSplitAnEmptyFlow() throws Exception {
        List<List<Integer>> result = Flows.<Integer>empty().split(x -> x == 0).runToList();
        assertEquals(List.of(), result);
    }

    @Test
    void shouldSplitAFlowWithNoDelimiters() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of(1, 2, 3)), result);
    }

    @Test
    void shouldSplitAFlowWithDelimiterAtTheBeginning() throws Exception {
        List<List<Integer>> result = Flows.fromValues(0, 1, 2, 3).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of(), List.of(1, 2, 3)), result);
    }

    @Test
    void shouldSplitAFlowWithDelimiterAtTheEnd() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 0).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of(1, 2, 3)), result);
    }

    @Test
    void shouldSplitAFlowWithDelimiterInTheMiddle() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 0, 3, 4).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of(1, 2), List.of(3, 4)), result);
    }

    @Test
    void shouldSplitAFlowWithMultipleDelimiters() throws Exception {
        List<List<Integer>> result = Flows.fromValues(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).split(x -> x % 4 == 0).runToList();
        assertEquals(List.of(
                List.of(),
                List.of(1, 2, 3),
                List.of(5, 6, 7),
                List.of(9)
        ), result);
    }

    @Test
    void shouldSplitAFlowWithAdjacentDelimiters() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 0, 0, 2, 3).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of(1), List.of(), List.of(2, 3)), result);
    }

    @Test
    void shouldSplitAFlowWithOnlyDelimiters() throws Exception {
        List<List<Integer>> result = Flows.fromValues(0, 0, 0).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of(), List.of(), List.of()), result);
    }

    @Test
    void shouldSplitAFlowWithSingleDelimiter() throws Exception {
        List<List<Integer>> result = Flows.fromValues(0).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of()), result);
    }

    @Test
    void shouldSplitAFlowWithSingleNonDelimiter() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of(1)), result);
    }

    @Test
    void shouldSplitAFlowWithMultipleConsecutiveDelimitersAtTheBeginning() throws Exception {
        List<List<Integer>> result = Flows.fromValues(0, 0, 1, 2).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of(), List.of(), List.of(1, 2)), result);
    }

    @Test
    void shouldSplitAFlowWithMultipleConsecutiveDelimitersAtTheEnd() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 0, 0).split(x -> x == 0).runToList();
        assertEquals(List.of(List.of(1, 2), List.of()), result);
    }

    @Test
    void shouldSplitAFlowWithStringDelimiters() throws Exception {
        List<List<String>> result = Flows.fromValues("a", ",", "b", "c", ",", "d").split(x -> x.equals(",")).runToList();
        assertEquals(List.of(
                List.of("a"),
                List.of("b", "c"),
                List.of("d")
        ), result);
    }

    @Test
    void shouldSplitAFlowUsingComplexPredicate() throws Exception {
        List<List<String>> result = Flows.fromValues("apple", "BANANA", "cherry", "DATES", "elderberry")
                .split(s -> s.chars().allMatch(Character::isUpperCase))
                .runToList();
        assertEquals(List.of(
                List.of("apple"),
                List.of("cherry"),
                List.of("elderberry")
        ), result);
    }

    @Test
    void shouldHandleErrorPropagationInSplit() throws Exception {
        RuntimeException exception = new RuntimeException("test error");
        Scopes.unsupervised(scope -> {
            ChannelError received = (ChannelError) Flows.failed(exception)
                    .split(x -> x.equals(0))
                    .runToChannel(scope)
                    .receiveOrClosed();
            assertEquals(exception, received.cause());
            return null;
        });
    }

    @Test
    void shouldSplitALargeFlowEfficiently() throws Exception {
        Flow<Integer> largeFlow = Flows.fromIterable(() -> java.util.stream.IntStream.range(1, 10001).iterator());
        List<List<Integer>> result = largeFlow.split(x -> x % 100 == 0).runToList();
        
        assertEquals(100, result.size()); // 100 chunks (delimiters at 100, 200, ..., 10000)
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99), result.get(0));
        assertEquals(List.of(101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199), result.get(1));
        assertEquals(List.of(9901, 9902, 9903, 9904, 9905, 9906, 9907, 9908, 9909, 9910, 9911, 9912, 9913, 9914, 9915, 9916, 9917, 9918, 9919, 9920, 9921, 9922, 9923, 9924, 9925, 9926, 9927, 9928, 9929, 9930, 9931, 9932, 9933, 9934, 9935, 9936, 9937, 9938, 9939, 9940, 9941, 9942, 9943, 9944, 9945, 9946, 9947, 9948, 9949, 9950, 9951, 9952, 9953, 9954, 9955, 9956, 9957, 9958, 9959, 9960, 9961, 9962, 9963, 9964, 9965, 9966, 9967, 9968, 9969, 9970, 9971, 9972, 9973, 9974, 9975, 9976, 9977, 9978, 9979, 9980, 9981, 9982, 9983, 9984, 9985, 9986, 9987, 9988, 9989, 9990, 9991, 9992, 9993, 9994, 9995, 9996, 9997, 9998, 9999), result.get(99));
    }

    // endregion

    // region splitOn tests

    @Test
    void shouldSplitOnAnEmptyFlow() throws Exception {
        List<List<Integer>> result = Flows.<Integer>empty().splitOn(List.of(0, 0)).runToList();
        assertEquals(List.of(), result);
    }

    @Test
    void shouldSplitOnAFlowWithNoDelimiters() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4).splitOn(List.of(0, 0)).runToList();
        assertEquals(List.of(List.of(1, 2, 3, 4)), result);
    }

    @Test
    void shouldSplitOnAFlowWithDelimiterAtTheBeginning() throws Exception {
        List<List<Integer>> result = Flows.fromValues(0, 0, 1, 2, 3).splitOn(List.of(0, 0)).runToList();
        assertEquals(List.of(List.of(), List.of(1, 2, 3)), result);
    }

    @Test
    void shouldSplitOnAFlowWithDelimiterAtTheEnd() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 0, 0).splitOn(List.of(0, 0)).runToList();
        assertEquals(List.of(List.of(1, 2, 3)), result);
    }

    @Test
    void shouldSplitOnAFlowWithDelimiterInTheMiddle() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 0, 0, 3, 4, 0, 0, 5).splitOn(List.of(0, 0)).runToList();
        assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), result);
    }

    @Test
    void shouldSplitOnAFlowWithAdjacentDelimiters() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 0, 0, 0, 0, 2, 3).splitOn(List.of(0, 0)).runToList();
        assertEquals(List.of(List.of(1), List.of(), List.of(2, 3)), result);
    }

    @Test
    void shouldSplitOnAFlowWithOnlyDelimiters() throws Exception {
        List<List<Integer>> result = Flows.fromValues(0, 0, 0, 0).splitOn(List.of(0, 0)).runToList();
        assertEquals(List.of(List.of(), List.of()), result);
    }

    @Test
    void shouldSplitOnAFlowWithSingleDelimiterSequence() throws Exception {
        List<List<Integer>> result = Flows.fromValues(0, 0).splitOn(List.of(0, 0)).runToList();
        assertEquals(List.of(List.of()), result);
    }

    @Test
    void shouldSplitOnAFlowWithSingleNonDelimiterElement() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1).splitOn(List.of(0, 0)).runToList();
        assertEquals(List.of(List.of(1)), result);
    }

    @Test
    void shouldSplitOnAFlowWithEmptyDelimiter() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 3, 4).splitOn(List.of()).runToList();
        assertEquals(List.of(List.of(1, 2, 3, 4)), result);
    }

    @Test
    void shouldSplitOnAFlowWithSingleElementDelimiter() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 0, 2, 3, 0, 4).splitOn(List.of(0)).runToList();
        assertEquals(List.of(List.of(1), List.of(2, 3), List.of(4)), result);
    }

    @Test
    void shouldSplitOnAFlowWithLongDelimiterSequence() throws Exception {
        List<List<Integer>> result = Flows.fromValues(1, 2, 9, 8, 7, 3, 4, 9, 8, 7, 5).splitOn(List.of(9, 8, 7)).runToList();
        assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), result);
    }

    @Test
    void shouldSplitOnAFlowWithStringDelimiters() throws Exception {
        List<List<String>> result = Flows.fromValues("a", "b", "--", "--", "c", "d", "--", "--", "e")
                .splitOn(List.of("--", "--"))
                .runToList();
        assertEquals(List.of(
                List.of("a", "b"),
                List.of("c", "d"),
                List.of("e")
        ), result);
    }

    @Test
    void shouldSplitOnAFlowWithOverlappingPatterns() throws Exception {
        // Test case where the delimiter pattern could potentially overlap
        List<List<Integer>> result = Flows.fromValues(1, 0, 1, 0, 1, 2, 3).splitOn(List.of(0, 1)).runToList();
        assertEquals(List.of(List.of(1), List.of(), List.of(2, 3)), result);
    }

    @Test
    void shouldHandleErrorPropagationInSplitOn() throws Exception {
        RuntimeException exception = new RuntimeException("test error");
        Scopes.unsupervised(scope -> {
            ChannelError received = (ChannelError) Flows.failed(exception)
                    .splitOn(List.of(0, 0))
                    .runToChannel(scope)
                    .receiveOrClosed();
            assertEquals(exception, received.cause());
            return null;
        });
    }

    @Test
    void shouldSplitOnALargeFlowEfficiently() throws Exception {
        // Create a large flow with periodic delimiter sequences
        List<Integer> data = new java.util.ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            data.add(i);
            if (i % 100 == 0) {
                data.add(-1);
                data.add(-1);
            }
        }
        
        Flow<Integer> largeFlow = Flows.fromIterable(data);
        List<List<Integer>> result = largeFlow.splitOn(List.of(-1, -1)).runToList();
        
        assertEquals(10, result.size()); // 10 chunks
        assertEquals(100, result.get(0).size()); // First chunk has 100 elements
        assertEquals(100, result.get(9).size()); // Last chunk has 100 elements
        assertEquals(Integer.valueOf(1), result.get(0).get(0)); // First element of first chunk
        assertEquals(Integer.valueOf(100), result.get(0).get(99)); // Last element of first chunk
    }

    // endregion
} 