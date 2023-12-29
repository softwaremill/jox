package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import static com.softwaremill.jox.Select.select;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelectTest {
    @Test
    public void testSimpleSelect() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch2.send("v");

        // when
        String received = select(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals("v", received);
    }
}
