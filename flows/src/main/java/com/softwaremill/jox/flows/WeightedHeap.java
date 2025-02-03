package com.softwaremill.jox.flows;

import java.util.*;

class WeightedHeap<T> {
    public record HeapNode<T>(
            T item,
            long weight
    ) {
        private HeapNode<T> copy() {
            return new HeapNode<>(item, weight);
        }
    }

    private final List<HeapNode<T>> heap;
    private final Map<T, Integer> valueToIndex;

    private WeightedHeap(List<HeapNode<T>> heap, Map<T, Integer> valueToIndex) {
        this.heap = heap;
        this.valueToIndex = valueToIndex;
    }

    public WeightedHeap() {
        this.heap = new ArrayList<>();
        this.valueToIndex = new HashMap<>();
    }

    public WeightedHeap<T> insert(T item, long weight) {
        if (valueToIndex.containsKey(item)) {
            return updateWeight(item, weight);
        }
        var newHeap = new ArrayList<>(heap);
        newHeap.add(new HeapNode<>(item, weight));

        var newValuesToIndex = new HashMap<>(valueToIndex);
        newValuesToIndex.put(item, newHeap.size() - 1);

        var result = bubbleUp(newHeap, newValuesToIndex, newHeap.size() - 1);
        return new WeightedHeap<>(result.getKey(), result.getValue());
    }

    public WeightedHeap<T> updateWeight(T item, long newWeight) {
        if (!valueToIndex.containsKey(item)) {
            throw new NoSuchElementException("Item %s not found in the heap".formatted(item));
        }
        int index = valueToIndex.get(item);
        HeapNode<T> node = heap.get(index);
        long oldWeight = node.weight;

        if (newWeight == oldWeight) {
            return this;
        }

        List<HeapNode<T>> newHeap = new ArrayList<>(heap);
        newHeap.set(index, new HeapNode<>(item, newWeight));

        Map.Entry<List<HeapNode<T>>, Map<T, Integer>> result;
        if (newWeight < oldWeight) {
            result = bubbleUp(newHeap, valueToIndex, index);
        } else {
            result = bubbleDown(newHeap, valueToIndex, index);
        }
        return new WeightedHeap<>(result.getKey(), result.getValue());
    }

    public Map.Entry<Optional<HeapNode<T>>, WeightedHeap<T>> extractMin() {
        if (heap.isEmpty()) {
            return Map.entry(Optional.empty(), this);
        } else if (heap.size() == 1) {
            return Map.entry(
                    Optional.of(heap.getFirst().copy()),
                    new WeightedHeap<>());
        }
        var min = heap.getFirst();
        var last = heap.getLast();

        var newHeap = new ArrayList<>(heap);
        newHeap.set(0, last);
        newHeap.removeLast();

        var newValueToIndex = new HashMap<>(valueToIndex);
        newValueToIndex.put(last.item, 0);
        newValueToIndex.remove(min.item);

        var result = bubbleDown(newHeap, newValueToIndex, 0);
        return Map.entry(
                Optional.of(min.copy()),
                new WeightedHeap<>(result.getKey(), result.getValue()));
    }

    public Optional<HeapNode<T>> peekMin() {
        if (heap.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(heap.getFirst().copy());
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    public boolean contains(T item) {
        return valueToIndex.containsKey(item);
    }

    private int parentIndex(int i) {
        return (i - 1) / 2;
    }

    private int leftChildIndex(int i) {
        return 2 * i + 1;
    }

    private int rightChildIndex(int i) {
        return 2 * i + 2;
    }

    private Map.Entry<List<HeapNode<T>>, Map<T, Integer>> swap(List<HeapNode<T>> heap, Map<T, Integer> valueToIndex, int i, int j) {
        List<HeapNode<T>> newHeap = new ArrayList<>(heap);
        Collections.swap(newHeap, i, j);

        Map<T, Integer> newValueToIndex = new HashMap<>(valueToIndex);
        newValueToIndex.put(heap.get(i).item, j);
        newValueToIndex.put(heap.get(j).item, i);

        return Map.entry(newHeap, newValueToIndex);
    }

    private Map.Entry<List<HeapNode<T>>, Map<T, Integer>> bubbleUp(List<HeapNode<T>> heap, Map<T, Integer> valueToIndex, int i) {
        int currentIndex = i;
        List<HeapNode<T>> currentHeap = heap;
        Map<T, Integer> currentMap = valueToIndex;

        while (currentIndex > 0 && currentHeap.get(currentIndex).weight < currentHeap.get(parentIndex(currentIndex)).weight) {
            var result = swap(currentHeap, currentMap, currentIndex, parentIndex(currentIndex));

            currentHeap = result.getKey();
            currentMap = result.getValue();
            currentIndex = parentIndex(currentIndex);
        }
        return Map.entry(currentHeap, currentMap);
    }

    private Map.Entry<List<HeapNode<T>>, Map<T, Integer>> bubbleDown(List<HeapNode<T>> heap, Map<T, Integer> valueToIndex, int i) {
        int currentIndex = i;
        var currentHeap = heap;
        Map<T, Integer> currentMap = valueToIndex;

        while (true) {
            int left = leftChildIndex(currentIndex);
            int right = rightChildIndex(currentIndex);
            int smallest = currentIndex;

            if (left < currentHeap.size() && currentHeap.get(left).weight < currentHeap.get(smallest).weight) {
                smallest = left;
            }
            if (right < currentHeap.size() && currentHeap.get(right).weight < currentHeap.get(smallest).weight) {
                smallest = right;
            }

            if (smallest == currentIndex) {
                return Map.entry(currentHeap, currentMap);
            }

            var result = swap(currentHeap, currentMap, currentIndex, smallest);
            currentHeap = result.getKey();
            currentMap = result.getValue();
            currentIndex = smallest;
        }
    }
}