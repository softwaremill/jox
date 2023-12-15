package jox;

public sealed class ChannelClosedException extends RuntimeException permits ChannelClosedException.ChannelDoneException, ChannelClosedException.ChannelErrorException {
    public ChannelClosedException() {
    }

    public ChannelClosedException(Throwable cause) {
        super(cause);
    }

    public static final class ChannelDoneException extends ChannelClosedException {
        public ChannelDoneException() {
        }
    }

    public static final class ChannelErrorException extends ChannelClosedException {
        public ChannelErrorException() {
        }

        public ChannelErrorException(Throwable cause) {
            super(cause);
        }
    }
}
