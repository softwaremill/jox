package com.softwaremill.jox.ops;

import com.softwaremill.jox.Channel;
import com.softwaremill.jox.ChannelDone;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class SourceOpsCollectTest {
    @Test
    void testMapOverSource() throws Exception {
        Scopes.supervised(scope -> {
            Channel<Integer> c = new Channel();
            scope.fork(() -> {
                c.send(1);
                c.send(2);
                c.send(3);
                c.done();
                return null;
            });

            Source<Integer> s = SourceOps.forSource(scope, c).collect(x -> x * 10).toSource();

            assertEquals(10, s.receive());
            assertEquals(20, s.receive());
            assertEquals(30, s.receive());
            assertInstanceOf(ChannelDone.class, s.receiveOrClosed());
            return null;
        });
    }

    @Test
    void testCollectOverSource() throws Exception {
        Scopes.supervised(scope -> {
            Channel<Integer> c = new Channel();
            scope.fork(() -> {
                c.send(1);
                c.send(2);
                c.send(3);
                c.send(4);
                c.send(5);
                c.done();
                return null;
            });

            Source<Integer> s = SourceOps.forSource(scope, c).collect(x -> {
                if (x % 2 == 0) return x * 10;
                else return null;
            }).toSource();

            assertEquals(20, s.receive());
            assertEquals(40, s.receive());
            assertInstanceOf(ChannelDone.class, s.receiveOrClosed());
            return null;
        });
    }

    @Test
    void testCollectOverSourceStressTest() throws Exception {
        for (int i = 0; i < 100000; i++) {
            Scopes.supervised(scope -> {
                Channel<Integer> c = new Channel();
                scope.fork(() -> {
                    c.send(1);
                    c.done();
                    return null;
                });

                Source<Integer> s = SourceOps.forSource(scope, c).collect(x -> x * 10).toSource();

                assertEquals(10, s.receive());
                assertInstanceOf(ChannelDone.class, s.receiveOrClosed());
                return null;
            });
        }
    }

    @Test
    void testCollectOverSourceUsingForSyntax() throws Exception {
        Scopes.supervised(scope -> {
            Channel<Integer> c = new Channel();
            scope.fork(() -> {
                c.send(1);
                c.send(2);
                c.send(3);
                c.done();
                return null;
            });

            Source<Integer> s = SourceOps.forSource(scope, c).collect(x -> x * 2).toSource();

            assertEquals(2, s.receive());
            assertEquals(4, s.receive());
            assertEquals(6, s.receive());
            assertInstanceOf(ChannelDone.class, s.receiveOrClosed());
            return null;
        });
    }
}
