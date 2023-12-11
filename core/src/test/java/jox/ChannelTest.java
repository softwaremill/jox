package jox;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static jox.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Channel tests which are run for various capacities.
 */
public class ChannelTest {
    @ChannelMultiTest
    void testSendReceiveInManyForks(int capacity) throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel = new Channel<>(capacity);
        var fs = new HashSet<Future<Void>>();
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        scoped(scope -> {
            for (int i = 1; i <= 1000; i++) {
                int ii = i;
                forkVoid(scope, () -> channel.send(ii));
            }
            for (int i = 1; i <= 1000; i++) {
                fs.add(forkVoid(scope, () -> s.add(channel.receive())));
            }
            for (Future<Void> f : fs) {
                f.get();
            }

            // then
            assertEquals(1000, s.size());
        });
    }

    @ChannelMultiTest
    void testSendReceiveManyElementsInTwoForks(int capacity) throws ExecutionException, InterruptedException {
        // given
        Channel<Integer> channel = new Channel<>(capacity);
        var s = new ConcurrentSkipListSet<Integer>();

        // when
        scoped(scope -> {
            forkVoid(scope, () -> {
                for (int i = 1; i <= 1000; i++) {
                    channel.send(i);
                }
            });
            forkVoid(scope, () -> {
                for (int i = 1; i <= 1000; i++) {
                    s.add(channel.receive());
                }
            }).get();

            // then
            assertEquals(1000, s.size());
        });
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
@ValueSource(strings = {"0", "1", "2", "10"})
@interface ChannelMultiTest {}
