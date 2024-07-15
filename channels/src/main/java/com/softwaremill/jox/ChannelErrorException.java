package com.softwaremill.jox;

public final class ChannelErrorException extends ChannelClosedException {
    public ChannelErrorException(Throwable cause) {
        super(cause);
    }
}
