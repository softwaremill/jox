package jox;

public sealed interface ChannelClosed permits ChannelClosed.ChannelDone, ChannelClosed.ChannelError {
    ChannelClosedException toException();

    final class ChannelDone implements ChannelClosed {
        public ChannelDone() {
        }

        @Override
        public ChannelClosedException toException() {
            return new ChannelClosedException.ChannelDoneException();
        }
    }

    final class ChannelError implements ChannelClosed {
        private final Throwable cause;

        public ChannelError(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public ChannelClosedException toException() {
            return new ChannelClosedException.ChannelErrorException(cause);
        }
    }
}
