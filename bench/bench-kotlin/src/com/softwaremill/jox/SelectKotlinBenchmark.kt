package com.softwaremill.jox

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

// same parameters as in the java benchmark
@Warmup(iterations = 3, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 5100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class SelectKotlinBenchmark {
    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    fun selectWithSingleClause_defaultDispatcher() {
        runBlocking {
            val channel = Channel<Long>(0)
            launch(Dispatchers.Default) {
                for (x in 1..OPERATIONS_PER_INVOCATION) channel.send(63)
                channel.close()
            }

            launch(Dispatchers.Default) {
                for (x in 1..OPERATIONS_PER_INVOCATION) select { channel.onReceive { x -> x } }
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    fun selectWithTwoClauses_defaultDispatcher() {
        runBlocking {
            val channel1 = Channel<Long>(0)
            val channel2 = Channel<Long>(0)
            launch(Dispatchers.Default) {
                for (x in 1..OPERATIONS_PER_INVOCATION / 2) channel1.send(63)
            }

            launch(Dispatchers.Default) {
                for (x in 1..OPERATIONS_PER_INVOCATION / 2) channel2.send(63)
            }

            launch(Dispatchers.Default) {
                for (x in 1..OPERATIONS_PER_INVOCATION) select {
                    channel1.onReceive { x -> x }
                    channel2.onReceive { x -> x }
                }
            }
        }
    }
}
