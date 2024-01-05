package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import static com.softwaremill.jox.Select.*;
import static com.softwaremill.jox.TestUtil.*;
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
}
