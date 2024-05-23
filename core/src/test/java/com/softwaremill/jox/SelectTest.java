package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import static com.softwaremill.jox.Select.defaultClause;
import static com.softwaremill.jox.Select.select;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SelectTest {
    @Test
    public void testSelectDefault() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);

        // when
        String received = select(ch1.receiveClause(), ch2.receiveClause(), defaultClause("x"));

        // then
        assertEquals("x", received);
    }

    @Test
    public void testDoNotSelectDefault() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);
        ch2.send("a");

        // when
        String received = select(ch1.receiveClause(), ch2.receiveClause(), defaultClause("x"));

        // then
        assertEquals("a", received);
    }

    @Test
    public void testDefaultCanOnlyBeLast() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<String> ch2 = new Channel<>(1);

        // when
        try {
            select(ch1.receiveClause(), defaultClause("x"), ch2.receiveClause());
            fail("Select should have failed");
        } catch (IllegalArgumentException e) {
            // then ok
        }
    }

    @Test
    public void testSelectObject() throws InterruptedException {
        // given
        Channel<String> ch1 = new Channel<>(1);
        Channel<Integer> ch2 = new Channel<>(1);
        ch1.send("x");

        // when
        Object received = select(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals("x", received);
    }
}
