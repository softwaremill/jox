package com.softwaremill.jox;

public sealed interface ChannelClosed permits ChannelDone, ChannelError {
    ChannelClosedException toException();
}
