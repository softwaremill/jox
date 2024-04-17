package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import static com.softwaremill.jox.Select.selectOrClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class CollectSourceTest {
    @Test
    void shouldReceiveFromCollectSource() throws InterruptedException {
        // given
        var c = Channel.<Integer>newUnlimitedChannel();
        var s = c.collectAsView(i -> i * 2);

        // when
        c.send(8);
        c.done();
        var r1 = s.receiveOrClosed();
        var r2 = s.receiveOrClosed();

        // then
        assertEquals(16, r1);
        assertInstanceOf(ChannelDone.class, r2);
    }

    @Test
    void shouldReceiveFromFilteredSource() throws InterruptedException {
        // given
        var c = Channel.<Integer>newUnlimitedChannel();
        var s = c.filterAsView(i -> i % 2 == 0);

        // when
        c.send(8);
        c.send(9);
        c.send(10);
        c.done();
        var r1 = s.receiveOrClosed();
        var r2 = s.receiveOrClosed();
        var r3 = s.receiveOrClosed();

        // then
        assertEquals(8, r1);
        assertEquals(10, r2);
        assertInstanceOf(ChannelDone.class, r3);
    }

    @Test
    void shouldReceiveFromCollectSourceInSelect() throws InterruptedException {
        // given
        var c = Channel.<Integer>newUnlimitedChannel();
        var s = c.collectAsView(i -> i * 2);

        // when
        c.send(8);
        c.done();
        var r1 = selectOrClosed(s.receiveClause());
        var r2 = selectOrClosed(s.receiveClause());

        // then
        assertEquals(16, r1);
        assertInstanceOf(ChannelDone.class, r2);
    }

    @Test
    void shouldReceiveFromFilteredSourceInSelect() throws InterruptedException {
        // given
        var c = Channel.<Integer>newUnlimitedChannel();
        var s = c.filterAsView(i -> i % 2 == 0);

        // when
        c.send(8);
        c.send(9);
        c.send(10);
        c.done();
        var r1 = selectOrClosed(s.receiveClause());
        var r2 = selectOrClosed(s.receiveClause());
        var r3 = selectOrClosed(s.receiveClause());

        // then
        assertEquals(8, r1);
        assertEquals(10, r2);
        assertInstanceOf(ChannelDone.class, r3);
    }
}
