package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import scala.Int;

public class FlowZipTest {

    @Test
    void shouldNotZipAnythingFromEmptyFlow() throws Exception {
        // given
        Flow<Integer> c = Flows.empty();
        Flow<Map.Entry<Integer, Long>> s = c.zipWithIndex();

        // when & then
        assertEquals(Collections.emptyList(), s.runToList());
    }

    @Test
    void shouldZipFlowWithIndex() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 2, 3, 4, 5);
        List<Map.Entry<Integer, Long>> expected = Arrays.asList(
                Map.entry(1, 0L), Map.entry(2, 1L), Map.entry(3, 2L), Map.entry(4, 3L), Map.entry(5, 4L)
        );

        // when & then
        Flow<Map.Entry<Integer, Long>> s = c.zipWithIndex();
        assertEquals(expected, s.runToList());
    }

    @Test
    void zipAll_shouldNotEmitAnyElementWhenBothFlowsAreEmpty() throws Exception {
        // given
        Flow<Integer> s = Flows.empty();
        Flow<String> other = Flows.empty();

        // when & then
        List<Map.Entry<Integer, String>> result = s.zipAll(other, -1, "foo").runToList();
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void zipAll_shouldEmitThisElementWhenOtherFlowIsEmpty() throws Exception {
        // given
        Flow<Integer> s = Flows.fromValues(1);
        Flow<String> other = Flows.empty();

        // when & then
        List<Map.Entry<Integer, String>> result = s.zipAll(other, -1, "foo").runToList();
        assertEquals(List.of(Map.entry(1, "foo")), result);
    }

    @Test
    void zipAll_shouldEmitOtherElementWhenThisFlowIsEmpty() throws Exception {
        // given
        Flow<Integer> s = Flows.empty();
        Flow<String> other = Flows.fromValues("a");

        // when & then
        List<Map.Entry<Integer, String>> result = s.zipAll(other, -1, "foo").runToList();
        assertEquals(List.of(Map.entry(-1, "a")), result);
    }

    @Test
    void zipAll_shouldEmitMatchingElementsWhenBothFlowsAreOfTheSameSize() throws Exception {
        // given
        Flow<Integer> s = Flows.fromValues(1, 2);
        Flow<String> other = Flows.fromValues("a", "b");

        // when & then
        List<Map.Entry<Integer, String>> result = s.zipAll(other, -1, "foo").runToList();
        assertEquals(List.of(Map.entry(1, "a"), Map.entry(2, "b")), result);
    }

    @Test
    void zipAll_shouldEmitDefaultForOtherFlowIfThisFlowIsLonger() throws Exception {
        // given
        Flow<Integer> s = Flows.fromValues(1, 2, 3);
        Flow<String> other = Flows.fromValues("a");

        // when & then
        List<Map.Entry<Integer, String>> result = s.zipAll(other, -1, "foo").runToList();
        assertEquals(List.of(Map.entry(1, "a"), Map.entry(2, "foo"), Map.entry(3, "foo")), result);
    }

    @Test
    void zipAll_shouldEmitDefaultForThisFlowIfOtherFlowIsLonger() throws Exception {
        // given
        Flow<Integer> s = Flows.fromValues(1);
        Flow<String> other = Flows.fromValues("a", "b", "c");

        // when & then
        List<Map.Entry<Integer, String>> result = s.zipAll(other, -1, "foo").runToList();
        assertEquals(List.of(Map.entry(1, "a"), Map.entry(-1, "b"), Map.entry(-1, "c")), result);
    }

    @Test
    void zip_shouldZipTwoSources() throws Exception {
        // given
        Flow<Integer> c1 = Flows.fromValues(1, 2, 3, 0);
        Flow<Integer> c2 = Flows.fromValues(4, 5, 6);
        List<Map.Entry<Integer, Integer>> expected = List.of(
                Map.entry(1, 4),
                Map.entry(2, 5),
                Map.entry(3, 6)
        );

        // when & then
        List<Map.Entry<Integer, Integer>> s = c1.zip(c2).runToList();
        assertEquals(expected, s);
    }
}
