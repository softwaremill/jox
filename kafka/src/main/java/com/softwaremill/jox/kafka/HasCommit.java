package com.softwaremill.jox.kafka;

import java.util.List;

/** Common interface for packets that contain messages to commit. */
public interface HasCommit {
    List<ReceivedMessage<?, ?>> commit();
}
