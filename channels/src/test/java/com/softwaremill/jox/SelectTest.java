package com.softwaremill.jox;

import static com.softwaremill.jox.Select.defaultClause;
import static com.softwaremill.jox.Select.defaultClauseNull;
import static com.softwaremill.jox.Select.select;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SelectTest {
    @Test
    public void testSelectDefaultValue() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);

        // when
        String received = select(ch1.receiveClause(), ch2.receiveClause(), defaultClause("x"));

        // then
        assertEquals("x", received);
    }

    @Test
    public void testSelectDefaultCallback() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);

        // when
        String received =
                select(ch1.receiveClause(), ch2.receiveClause(), defaultClause(() -> "x"));

        // then
        assertEquals("x", received);
    }

    @Test
    public void testSelectDefaultNull() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);

        // when
        String received = select(ch1.receiveClause(), ch2.receiveClause(), defaultClauseNull());

        // then
        assertNull(received);
    }

    @Test
    public void testDoNotSelectDefault() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);
        ch2.send("a");

        // when
        String received = select(ch1.receiveClause(), ch2.receiveClause(), defaultClause("x"));

        // then
        assertEquals("a", received);
    }

    @Test
    public void testDefaultCanOnlyBeLast() throws InterruptedException {
        // given
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<String> ch2 = Channel.newBufferedChannel(1);

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
        Channel<String> ch1 = Channel.newBufferedChannel(1);
        Channel<Integer> ch2 = Channel.newBufferedChannel(1);
        ch1.send("x");

        // when
        Object received = select(ch1.receiveClause(), ch2.receiveClause());

        // then
        assertEquals("x", received);
    }
}
