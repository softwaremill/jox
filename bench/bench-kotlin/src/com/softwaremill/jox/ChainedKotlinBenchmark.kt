package com.softwaremill.jox

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

// same parameters as in the java benchmark
@Warmup(iterations = 3, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
open class ChainedKotlinBenchmark {
    @Param("0", "100")
    var capacity: Int = 0

    @Param("100", "1000", "10000")
    var chainLength: Int = 0

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION_CHAINED)
    fun channelChain_defaultDispatcher() {
        runBlocking {
            // we want to measure the amount of time a send-receive pair takes
            var elements = OPERATIONS_PER_INVOCATION_CHAINED / chainLength

            // create an array of channelCount channels
            val channels = Array(chainLength) { Channel<Long>(capacity) }

            launch(Dispatchers.Default) {
                var ch = channels[0]
                for (x in 1..elements) ch.send(63)
            }

            for (t in 1 until chainLength) {
                val ch1 = channels[t - 1]
                val ch2 = channels[t]
                launch(Dispatchers.Default) {
                    for (x in 1..elements) ch2.send(ch1.receive())
                }
            }

            launch(Dispatchers.Default) {
                var ch = channels[chainLength - 1]
                for (x in 1..elements) ch.receive()
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION_CHAINED)
    fun channelChain_eventLoop() {
        runBlocking {
            // we want to measure the amount of time a send-receive pair takes
            var elements = OPERATIONS_PER_INVOCATION_CHAINED / chainLength

            // create an array of channelCount channels
            val channels = Array(chainLength) { Channel<Long>(capacity) }

            launch {
                var ch = channels[0]
                for (x in 1..elements) ch.send(63)
            }

            for (t in 1 until chainLength) {
                val ch1 = channels[t - 1]
                val ch2 = channels[t]
                launch {
                    for (x in 1..elements) ch2.send(ch1.receive())
                }
            }

            launch {
                var ch = channels[chainLength - 1]
                for (x in 1..elements) ch.receive()
            }
        }
    }
}
