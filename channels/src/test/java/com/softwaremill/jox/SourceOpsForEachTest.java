package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class SourceOpsForEachTest {
    @Test
    void testIterateOverSource() throws Exception {
        var c = new Channel<Integer>(10);
        c.sendOrClosed(1);
        c.sendOrClosed(2);
        c.sendOrClosed(3);
        c.doneOrClosed();

        List<Integer> r = new ArrayList<>();
        c.forEach(v -> r.add(v));

        assertIterableEquals(List.of(1, 2, 3), r);
    }

    @Test
    void testConvertSourceToList() throws Exception {
        var c = new Channel<Integer>(10);
        c.sendOrClosed(1);
        c.sendOrClosed(2);
        c.sendOrClosed(3);
        c.doneOrClosed();

        List<Integer> resultList = c.toList();
        assertEquals(List.of(1, 2, 3), resultList);
    }
}
