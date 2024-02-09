package com.softwaremill.jox;

/**
 * A channel which can be closed.
 * <p>
 * A channel can be closed in two ways:
 * <ul>
 *     <li>using {@link #done()} or {@link #doneSafe()}, indicating that no more elements will be sent</li>
 *     <li>using {@link #error(Throwable)} or {@link #errorSafe(Throwable)}, indicating an error</li>
 * </ul>
 * <p>
 * A channel can be closed only once. Subsequent calls to {@link #done()} or {@link #error(Throwable)} will throw
 * {@link ChannelClosedException}, or return the original closing reason (when using {@link #doneSafe()} or {@link #errorSafe(Throwable)}).
 * <p>
 * Closing the channel is thread-safe.
 */
public interface CloseableChannel {
    /**
     * Close the channel, indicating that no more elements will be sent.
     * <p>
     * Any elements that are already buffered will be delivered. Any send operations that are in progress will complete
     * normally, when a receiver arrives. Any pending receive operations will complete with a channel closed result.
     * <p>
     * Subsequent {@link Sink#send(Object)} operations will throw {@link ChannelClosedException}.
     *
     * @throws ChannelClosedException When the channel is already closed.
     */
    void done();

    /**
     * Close the channel, indicating that no more elements will be sent. Doesn't throw exceptions when the channel is
     * closed, but returns a value.
     * <p>
     * Any elements that are already buffered will be delivered. Any send operations that are in progress will complete
     * normally, when a receiver arrives. Any pending receive operations will complete with a channel closed result.
     * <p>
     * Subsequent {@link Sink#send(Object)} operations will throw {@link ChannelClosedException}.
     *
     * @return Either {@code null}, or {@link ChannelClosed}, when the channel is already closed.
     */
    Object doneSafe();

    //

    /**
     * Close the channel, indicating an error.
     * <p>
     * Any elements that are already buffered won't be delivered. Any send or receive operations that are in progress
     * will complete with a channel closed result.
     * <p>
     * Subsequent {@link Sink#send(Object)} and {@link Source#receive()} operations will throw {@link ChannelClosedException}.
     *
     * @param reason The reason of the error. Not {@code null}.
     * @throws ChannelClosedException When the channel is already closed.
     */
    void error(Throwable reason);

    /**
     * Close the channel, indicating an error. Doesn't throw exceptions when the channel is closed, but returns a value.
     * <p>
     * Any elements that are already buffered won't be delivered. Any send or receive operations that are in progress
     * will complete with a channel closed result.
     * <p>
     * Subsequent {@link Sink#send(Object)} and {@link Source#receive()} operations will throw {@link ChannelClosedException}.
     *
     * @return Either {@code null}, or {@link ChannelClosed}, when the channel is already closed.
     */
    Object errorSafe(Throwable reason);

    //

    /**
     * @return {@code true} if no more values can be sent to this channel; {@link Sink#send(Object)} will throw
     * {@link ChannelClosedException} or return {@link ChannelClosed} (in the safe variant).
     * <p>
     * When closed for send, receiving using {@link Source#receive()} might still be possible, if the channel is done,
     * and not in an error. This can be verified using {@link #isClosedForReceive()}.
     */
    default boolean isClosedForSend() {
        return closedForSend() != null;
    }

    /**
     * @return {@code true} if no more values can be received from this channel; {@link Source#receive()} will throw
     * {@link ChannelClosedException} or return {@link ChannelClosed} (in the safe variant).
     * <p>
     * When closed for receive, sending values is also not possible, {@link #isClosedForSend()} will return {@code true}.
     */
    default boolean isClosedForReceive() {
        return closedForReceive() != null;
    }

    /**
     * @return Non-{@code null} if no more values can be sent to this channel; {@link Sink#send(Object)} will throw
     * {@link ChannelClosedException} or return {@link ChannelClosed} (in the safe variant).
     * <p>
     * {@code null} if the channel is not closed, and values can be sent.
     * <p>
     * When closed for send, receiving using {@link Source#receive()} might still be possible, if the channel is done,
     * and not in an error. This can be verified using {@link #isClosedForReceive()}.
     */
    ChannelClosed closedForSend();

    /**
     * @return Non-{@code null} if no more values can be received from this channel; {@link Source#receive()} will throw
     * {@link ChannelClosedException} or return {@link ChannelClosed} (in the safe variant).
     * <p>
     * {@code null} if the channel is not closed, and values can be received.
     * <p>
     * When closed for receive, sending values is also not possible, {@link #isClosedForSend()} will return {@code true}.
     */
    ChannelClosed closedForReceive();
}
