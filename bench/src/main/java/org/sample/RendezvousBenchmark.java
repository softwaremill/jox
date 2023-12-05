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

package org.sample;

import jox.Channel;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.Exchanger;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Rendezvous tests for {@link SynchronousQueue}, {@link Exchanger} and {@link Channel}.
 */
@Warmup(iterations = 3, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
// after the measurement time, we want to interrupt any pending methods (which might block, waiting for a partner)
// this needs to be slightly larger than the test time to avoid warnings
@Timeout(time = 5100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Group)
public class RendezvousBenchmark {
    private SynchronousQueue<Integer> queue = new SynchronousQueue<>();

    @Benchmark
    @Group("synchronous_queue")
    @GroupThreads(1)
    public void putToSynchronousQueue() throws InterruptedException {
        queue.put(63);
    }

    @Benchmark
    @Group("synchronous_queue")
    @GroupThreads(1)
    public void takeFromSynchronousQueue() throws InterruptedException {
        queue.take();
    }

    //

    private Exchanger<Integer> exchanger = new Exchanger<>();

    @Benchmark
    @Group("exchanger")
    @GroupThreads(1)
    public void exchange1() throws InterruptedException {
        exchanger.exchange(63);
    }

    @Benchmark
    @Group("exchanger")
    @GroupThreads(1)
    public void exchange2() throws InterruptedException {
        exchanger.exchange(64);
    }

    //

    private Channel<Integer> channel = new Channel<>();

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
}
