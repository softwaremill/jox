package com.softwaremill.jox;

public sealed interface ChannelClosed permits ChannelClosed.ChannelDone, ChannelClosed.ChannelError {
    ChannelClosedException toException();

    record ChannelDone() implements ChannelClosed {
        @Override
        public ChannelClosedException toException() {
            return new ChannelDoneException();
        }
    }

    record ChannelError(Throwable cause) implements ChannelClosed {
        @Override
        public ChannelClosedException toException() {
            return new ChannelErrorException(cause);
        }
    }
}
