package com.softwaremill.jox.structured;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Trail {
    private Queue<String> data = new ConcurrentLinkedQueue<>();

    public void add(String v) {
        data.add(v);
    }

    public List<String> get() {
        return data.stream().toList();
    }
}
