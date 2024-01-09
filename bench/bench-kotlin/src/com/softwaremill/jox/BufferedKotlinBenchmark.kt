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
@Timeout(time = 5100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
open class BufferedKotlinBenchmark {
    @Param("1", "10", "100")
    var capacity: Int = 0

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION)
    fun channel_defaultDispatcher() {
        runBlocking {
            val channel = Channel<Long>(capacity)
            launch(Dispatchers.Default) {
                for (x in 1..OPERATIONS_PER_INVOCATION) channel.send(63)
                channel.close()
            }

            launch(Dispatchers.Default) {
                for (x in 1..OPERATIONS_PER_INVOCATION) channel.receive()
            }
        }
    }
}
