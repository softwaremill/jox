package com.softwaremill.jox.flows;

import com.softwaremill.jox.flows.WeightedHeap.HeapNode;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class WeightedHeapTest {

    @Test
    void testInsertElementsWithWeights() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        heap = heap.insert("A", 5L);
        heap = heap.insert("B", 3L);
        heap = heap.insert("C", 8L);

        assertEquals(3, heap.size());
        assertEquals(new HeapNode<>("B", 3L), heap.peekMin().orElse(null));
    }

    @Test
    void testExtractMinElement() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        heap = heap.insert("A", 5L);
        heap = heap.insert("B", 3L);
        heap = heap.insert("C", 8L);

        var newHeap = heap.extractMin();
        heap = newHeap.getValue();
        assertEquals(new HeapNode<>("B", 3L), newHeap.getKey().orElse(null));
        assertEquals(2, heap.size());
        assertEquals(new HeapNode<>("A", 5L), heap.peekMin().orElse(null));
    }

    @Test
    void testExtractFromEmptyHeap() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        assertNull(heap.extractMin().getKey().orElse(null));
    }

    @Test
    void testHeapSizeAfterOperations() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        assertEquals(0, heap.size());

        heap = heap.insert("A", 5L);
        heap = heap.insert("B", 3L);
        heap = heap.insert("C", 8L);
        assertEquals(3, heap.size());

        heap = heap.extractMin().getValue();
        assertEquals(2, heap.size());

        heap = heap.extractMin().getValue();
        heap = heap.extractMin().getValue();
        assertEquals(0, heap.size());
    }

    @Test
    void testEmptyHeapHandling() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());

        heap = heap.insert("A", 5L);
        assertFalse(heap.isEmpty());

        heap = heap.extractMin().getValue();
        assertTrue(heap.isEmpty());
    }

    @Test
    void testUpdateWeightAndAdjustPosition() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        heap = heap.insert("A", 5L);
        heap = heap.insert("B", 3L);
        heap = heap.insert("C", 8L);

        heap = heap.updateWeight("A", 2L);
        assertEquals(new HeapNode<>("A", 2L), heap.peekMin().orElse(null));

        heap = heap.updateWeight("C", 1L);
        assertEquals(new HeapNode<>("C", 1L), heap.peekMin().orElse(null));
    }

    @Test
    void testUpdateWeightOfNonExistentElement() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        final var resultHeap = heap.insert("A", 5L);

        assertThrows(NoSuchElementException.class, () -> resultHeap.updateWeight("B", 3L));
    }

    @Test
    void testMultipleInsertionsAndUpdates() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        heap = heap.insert("A", 10L);
        heap = heap.insert("B", 15L);
        heap = heap.insert("C", 5L);
        heap = heap.insert("D", 20L);

        assertEquals(new HeapNode<>("C", 5L), heap.peekMin().orElse(null));

        heap = heap.updateWeight("A", 2L);
        assertEquals(new HeapNode<>("A", 2L), heap.peekMin().orElse(null));

        heap = heap.updateWeight("D", 1L);
        assertEquals(new HeapNode<>("D", 1L), heap.peekMin().orElse(null));

        assertEquals(4, heap.size());
    }

    @Test
    void testDuplicateInsertions() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        heap = heap.insert("A", 5L);
        heap = heap.insert("A", 2L);

        assertEquals(1, heap.size());
        assertEquals(new HeapNode<>("A", 2L), heap.peekMin().orElse(null));
    }

    @Test
    void testIncreaseWeightOfExistingElement() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        heap = heap.insert("A", 3L);
        heap = heap.insert("B", 2L);
        heap = heap.insert("C", 1L);

        assertEquals(new HeapNode<>("C", 1L), heap.peekMin().orElse(null));

        heap = heap.updateWeight("C", 5L);
        assertEquals(new HeapNode<>("B", 2L), heap.peekMin().orElse(null));

        heap = heap.extractMin().getValue();
        assertEquals(new HeapNode<>("A", 3L), heap.peekMin().orElse(null));

        heap = heap.extractMin().getValue();
        assertEquals(new HeapNode<>("C", 5L), heap.peekMin().orElse(null));
    }

    @Test
    void testMultipleWeightIncreases() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        heap = heap.insert("X", 1L);
        heap = heap.insert("Y", 2L);
        heap = heap.insert("Z", 3L);

        heap = heap.updateWeight("X", 6L);
        heap = heap.updateWeight("Y", 5L);

        assertEquals(new HeapNode<>("Z", 3L), heap.peekMin().orElse(null));

        heap = heap.extractMin().getValue();
        assertEquals(new HeapNode<>("Y", 5L), heap.peekMin().orElse(null));

        heap = heap.extractMin().getValue();
        assertEquals(new HeapNode<>("X", 6L), heap.peekMin().orElse(null));
    }

    @Test
    void testIncreaseWeightOfCurrentMinElement() {
        WeightedHeap<String> heap = new WeightedHeap<>();
        heap = heap.insert("P", 1L);
        heap = heap.insert("Q", 2L);
        heap = heap.insert("R", 3L);

        assertEquals(new HeapNode<>("P", 1L), heap.peekMin().orElse(null));

        heap = heap.updateWeight("P", 4L);
        assertEquals(new HeapNode<>("Q", 2L), heap.peekMin().orElse(null));

        heap = heap.extractMin().getValue();
        assertEquals(new HeapNode<>("R", 3L), heap.peekMin().orElse(null));

        heap = heap.extractMin().getValue();
        assertEquals(new HeapNode<>("P", 4L), heap.peekMin().orElse(null));
    }

    @Test
    void testLargeHeapWeightIncreases() {
        WeightedHeap<Integer> heap = new WeightedHeap<>();
        for (int i = 1; i <= 10; i++) {
            heap = heap.insert(i, i);
        }

        heap = heap.updateWeight(1, 15L);
        heap = heap.updateWeight(5, 12L);

        assertEquals(new HeapNode<>(2, 2L), heap.peekMin().orElse(null));

        List<HeapNode<Integer>> result = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            result.add(heap.extractMin().getKey().orElse(null));
            heap = heap.extractMin().getValue();
        }

        List<Integer> expectedOrder = Arrays.asList(2, 3, 4, 6, 7, 8, 9, 10, 5, 1);
        assertEquals(expectedOrder, result.stream().map(HeapNode::item).toList());
    }

    @Test
    void testMultipleOperations() {
        WeightedHeap<Integer> heap = new WeightedHeap<>();
        heap = heap.insert(10, 10L);
        heap = heap.insert(20, 20L);
        heap = heap.insert(30, 30L);
        heap = heap.insert(5, 5L);
        heap = heap.insert(15, 15L);

        var newHeap = heap.extractMin();
        heap = newHeap.getValue();
        assertEquals(new HeapNode<>(5, 5L), newHeap.getKey().orElse(null));

        newHeap = heap.extractMin();
        heap = newHeap.getValue();
        assertEquals(new HeapNode<>(10, 10L), newHeap.getKey().orElse(null));

        newHeap = heap.extractMin();
        heap = newHeap.getValue();
        assertEquals(new HeapNode<>(15, 15L), newHeap.getKey().orElse(null));

        newHeap = heap.extractMin();
        heap = newHeap.getValue();
        assertEquals(new HeapNode<>(20, 20L), newHeap.getKey().orElse(null));

        newHeap = heap.extractMin();
        heap = newHeap.getValue();
        assertEquals(new HeapNode<>(30, 30L), newHeap.getKey().orElse(null));

        newHeap = heap.extractMin();
        heap = newHeap.getValue();
        assertNull(heap.extractMin().getKey().orElse(null));
    }

    @Test
    void testLargeNumberOfElements() {
        WeightedHeap<Integer> heap = new WeightedHeap<>();
        for (int i = 1; i <= 1000; i++) {
            heap = heap.insert(i, 1000 - i);
        }

        assertEquals(1000, heap.size());
        assertEquals(new HeapNode<>(1000, 0L), heap.peekMin().orElse(null));

        for (int i = 1000; i >= 1; i--) {
            assertEquals(new HeapNode<>(i, 1000 - i), heap.extractMin().getKey().orElse(null));
            heap = heap.extractMin().getValue();
        }

        assertTrue(heap.isEmpty());
    }

    @Test
    void testRandomInsertionsAndExtractions() {
        // given
        Random random = new SecureRandom();
        WeightedHeap<Integer> heap = new WeightedHeap<>();
        var initialValues = IntStream.rangeClosed(1, 100)
                .boxed()
                .map(_ -> Map.entry(random.nextInt(1000), random.nextLong()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, _) -> a));

        var elements = initialValues.entrySet().stream()
                .map(e -> new HeapNode<>(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(HeapNode::weight))
                .toList();

        // when
        for (HeapNode<Integer> element : elements) {
            heap = heap.insert(element.item(), element.weight());
        }

        // then
        for (HeapNode<Integer> element : elements) {
            assertEquals(element, heap.peekMin().orElse(null));
            assertEquals(element, heap.extractMin().getKey().orElse(null));
            heap = heap.extractMin().getValue();
        }
        assertTrue(heap.isEmpty());
    }

    @Test
    void testRandomWeightUpdates() {
        // given
        Random random = new Random();
        WeightedHeap<Integer> heap = new WeightedHeap<>();
        List<Integer> elements = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            int value = random.nextInt(1000);
            elements.add(value);
            long weight = random.nextLong();
            heap = heap.insert(value, weight);
        }
        elements = new ArrayList<>(new HashSet<>(elements));

        // when
        List<HeapNode<Integer>> updatedElements = new ArrayList<>();
        for (Integer element : elements) {
            long newWeight = random.nextLong();
            heap = heap.updateWeight(element, newWeight);
            updatedElements.add(new HeapNode<>(element, newWeight));
        }
        updatedElements.sort(Comparator.comparingLong(HeapNode::weight));

        // then
        for (HeapNode<Integer> element : updatedElements) {
            var newHeap = heap.extractMin();
            assertEquals(element, newHeap.getKey().orElse(null));
            heap = newHeap.getValue();
        }

        assertTrue(heap.isEmpty());
    }
}