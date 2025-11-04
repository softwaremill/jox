package com.softwaremill.jox.fray;

class Config {
    static final int CHANNEL_SIZE;

    static {
        String channelSizeEnv = System.getenv("CHANNEL_SIZE");
        CHANNEL_SIZE = channelSizeEnv != null ? Integer.parseInt(channelSizeEnv) : 16;
        System.out.println("Using CHANNEL_SIZE: " + CHANNEL_SIZE);
    }
}
