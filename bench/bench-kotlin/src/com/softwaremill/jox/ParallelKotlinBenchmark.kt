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
open class ParallelKotlinBenchmark {
    @Param("0", "16", "100")
    var capacity: Int = 0

    @Param("10000")
    var parallelism: Int = 0

    @Benchmark
    @OperationsPerInvocation(OPERATIONS_PER_INVOCATION_PARALLEL)
    fun parallelChannels_defaultDispatcher() {
        runBlocking {
            // we want to measure the amount of time a send-receive pair takes
            val elements = OPERATIONS_PER_INVOCATION_PARALLEL / parallelism

            for (t in 0 until parallelism) {
                val ch = Channel<Int>(capacity)

                // sender
                launch(Dispatchers.Default) {
                    for (x in 1..elements) ch.send(91)
                }

                // receiver
                launch(Dispatchers.Default) {
                    for (x in 1..elements) ch.receive()
                }
            }
        }
    }
}
