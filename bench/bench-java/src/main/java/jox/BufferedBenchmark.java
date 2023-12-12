/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package jox;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Buffered tests for {@link ArrayBlockingQueue} and {@link Channel}.
 */
@Warmup(iterations = 3, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
// after the measurement time, we want to interrupt any pending methods (which might block, waiting for a partner)
// this needs to be slightly larger than the test time to avoid warnings
@Timeout(time = 5100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Group)
public class BufferedBenchmark {
    @Param({"1", "10", "100"})
    public int capacity;

    private ArrayBlockingQueue<Integer> queue;
    private Channel<Integer> channel;

    @Setup
    public void create() {
        queue = new ArrayBlockingQueue<>(capacity);
        channel = new Channel<>(capacity);
    }

    //

    @Benchmark
    @Group("array_blocking_queue")
    @GroupThreads(1)
    public void putToArrayBlockingQueue() throws InterruptedException {
        queue.put(63);
    }

    @Benchmark
    @Group("array_blocking_queue")
    @GroupThreads(1)
    public void takeFromArrayBlockingQueue() throws InterruptedException {
        queue.take();
    }

    //

    @Benchmark
    @Group("channel")
    @GroupThreads(1)
    public void sendToChannel() throws InterruptedException {
        channel.send(63);
    }

    @Benchmark
    @Group("channel")
    @GroupThreads(1)
    public void receiveFromChannel() throws InterruptedException {
        channel.receive();
    }

    //

    // including an iterative benchmark, for direct comparison w/ Kotlin, as we can't write a group-based benchmark
    // there, due to suspended functions

    private final static int OPERATIONS_PER_INVOCATION = 1_000_000;

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    @Group("channel_iterative")
    public void sendReceive() throws InterruptedException {
        var t1 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    channel.send(63);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        var t2 = Thread.startVirtualThread(() -> {
            for (int i = 0; i < OPERATIONS_PER_INVOCATION; i++) {
                try {
                    channel.receive();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        t1.join();
        t2.join();
    }
}
