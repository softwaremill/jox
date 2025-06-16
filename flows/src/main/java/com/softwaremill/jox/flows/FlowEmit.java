package com.softwaremill.jox.flows;

import com.softwaremill.jox.ChannelDone;
import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.Source;

/**
 * Instances of this interface should be considered thread-unsafe, and only used within the scope in
 * which they have been obtained, e.g. as part of {@link Flows#usingEmit} or {@link
 * Flow#mapUsingEmit}.
 */
public interface FlowEmit<T> {

    /**
     * Emit a value to be processed downstream. Blocks until the value is fully processed, or throws
     * an exception if an error occurred.
     */
    void apply(T t) throws Exception;

    /**
     * Propagates all elements to the given emit. Completes once the channel completes as done.
     * Throws an exception if the channel transits to an error state.
     */
    static <T> void channelToEmit(Source<T> source, FlowEmit<T> emit) throws Exception {
        boolean shouldRun = true;
        while (shouldRun) {
            Object t = source.receiveOrClosed();
            shouldRun =
                    switch (t) {
                        case ChannelDone done -> false;
                        case ChannelError error -> throw error.toException();
                        default -> {
                            //noinspection unchecked
                            emit.apply((T) t);
                            yield true;
                        }
                    };
        }
    }
}
