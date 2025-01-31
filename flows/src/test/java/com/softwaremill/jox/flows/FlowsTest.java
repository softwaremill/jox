package com.softwaremill.jox.flows;

import com.softwaremill.jox.ChannelError;
import com.softwaremill.jox.Source;
import com.softwaremill.jox.structured.Fork;
import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FlowsTest {

    @Test
    void shouldBeEmpty() throws Exception {
        assertTrue(Flows.empty().runToList().isEmpty());
    }

    @Test
    void shouldCreateFlowFromFork() throws Exception {
        Scopes.unsupervised(scope -> {
            Fork<Integer> f = scope.forkUnsupervised(() -> 1);
            Flow<Integer> c = Flows.fromFork(f);
            assertEquals(List.of(1), c.runToList());
            return null;
        });
    }

    @Test
    void shouldCreateIteratingFlow() throws Exception {
        // given
        Flow<Integer> c = Flows.iterate(1, i -> i + 1);

        // when & then
        assertEquals(List.of(1, 2, 3), c.take(3).runToList());
    }

    @Test
    void shouldProduceRange() throws Exception {
        assertEquals(List.of(1, 2, 3, 4, 5), Flows.range(1, 5, 1).runToList());
        assertEquals(List.of(1, 3, 5), Flows.range(1, 5, 2).runToList());
        assertEquals(List.of(1, 4, 7, 10), Flows.range(1, 11, 3).runToList());
    }

    @Test
    public void shouldFailOnReceive() throws Exception {
        Scopes.unsupervised(scope -> {
            // when
            Flow<String> s = Flows.failed(new RuntimeException("boom"));

            // then
            Object received = s.runToChannel(scope).receiveOrClosed();
            assertInstanceOf(ChannelError.class, received);
            assertEquals("boom", ((ChannelError) received).cause().getMessage());
            return null;
        });
    }

    @Test
    void shouldReturnOriginalFutureFailureWhenFutureFails() {
        // given
        RuntimeException failure = new RuntimeException("future failed");

        // when & then
        ExecutionException exception = assertThrows(ExecutionException.class,
                () -> Flows.fromCompletableFuture(CompletableFuture.failedFuture(failure)).runToList());
        assertEquals(failure, exception.getCause());
    }

    @Test
    void shouldReturnFutureValue() throws Exception {
        // given
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(1);

        // when
        List<Integer> result = Flows.fromCompletableFuture(future).runToList();

        // then
        assertEquals(List.of(1), result);
    }

    @Test
    void shouldReturnFuturesSourceValues() throws Exception {
        Scopes.unsupervised(scope -> {
            // given
            CompletableFuture<Source<Integer>> completableFuture = CompletableFuture
                    .completedFuture(Flows.fromValues(1, 2).runToChannel(scope));

            // when
            List<Integer> result = Flows.fromFutureSource(completableFuture).runToList();

            // then
            assertEquals(List.of(1, 2), result);
            return null;
        });
    }

    @Test
    void shouldReturnOriginalFutureFailureWhenSourceFutureFails() {
        // given
        RuntimeException failure = new RuntimeException("future failed");

        // when & then
        assertThrows(RuntimeException.class,
                () -> Flows.fromFutureSource(CompletableFuture.failedFuture(failure)).runToList(),
                failure.getMessage());
    }

    @Test
    void shouldEvaluateElementBeforeEachSend() throws Exception {
        // given
        AtomicInteger i = new AtomicInteger(0);

        // when
        List<Integer> actual = Flows.repeatEval(i::incrementAndGet)
                                    .take(3)
                                    .runToList();

        // then
        assertEquals(List.of(1, 2, 3), actual);
    }

    @Test
    void shouldEvaluateElementBeforeEachSendAsLongAsDefined() throws Exception {
        // given
        AtomicInteger i = new AtomicInteger(0);
        Set<Integer> evaluated = new HashSet<>();

        // when
        List<Integer> result = Flows.repeatEvalWhileDefined(() -> {
                                        int value = i.incrementAndGet();
                                        evaluated.add(value);
                                        return value < 5 ? Optional.of(value) : Optional.empty();
                                    })
                                    .runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4), result);
        assertEquals(Set.of(1, 2, 3, 4, 5), evaluated);
    }

    @Test
    void shouldRepeatTheSameElement() throws Exception {
        // when
        List<Integer> result = Flows.repeat(2137)
                                    .take(3)
                                    .runToList();

        // then
        assertEquals(List.of(2137, 2137, 2137), result);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void shouldTickRegularly() throws InterruptedException {
        Scopes.unsupervised(scope -> {
            var c = Flows.tick(Duration.ofMillis(100), 1L)
                         .runToChannel(scope);
            var start = System.currentTimeMillis();

            c.receive();
            assertTrue(System.currentTimeMillis() - start >= 0);
            assertTrue(System.currentTimeMillis() - start <= 50);

            c.receive();
            assertTrue(System.currentTimeMillis() - start >= 100);
            assertTrue(System.currentTimeMillis() - start <= 150);

            c.receive();
            assertTrue(System.currentTimeMillis() - start >= 200);
            assertTrue(System.currentTimeMillis() - start <= 250);
            return null;
        });
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void shouldTickImmediatelyInCaseOfSlowConsumerAndThenResumeNormal() throws InterruptedException {
        Scopes.unsupervised(scope -> {
            var c = Flows.tick(Duration.ofMillis(100), 1L)
                         .runToChannel(scope);
            var start = System.currentTimeMillis();

            Thread.sleep(200);
            c.receive();
            assertTrue(System.currentTimeMillis() - start >= 200);
            assertTrue(System.currentTimeMillis() - start <= 250);

            c.receive();
            assertTrue(System.currentTimeMillis() - start >= 200);
            assertTrue(System.currentTimeMillis() - start <= 250);
            return null;
        });
    }

    @Test
    void shouldTimeout() throws Exception {
        // given
        long start = System.currentTimeMillis();
        Flow<Integer> c = Flows.concat(
                Flows.timeout(Duration.ofMillis(100)),
                Flows.fromValues(1)
        );

        // when
        List<Integer> result = c.runToList();

        // then
        assertEquals(List.of(1), result);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 100L);
        assertTrue(elapsed <= 150L);
    }

    @Test
    void shouldConcatFlows() throws Exception {
        // given
        Flow<Integer> flow = Flows.concat(
                Flows.fromValues(1, 2, 3),
                Flows.fromValues(4, 5, 6)
        );

        // when
        List<Integer> result = flow.runToList();

        // then
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    void shouldInterleaveNoSources() throws Exception {
        // when
        List<Integer> actual = Flows.<Integer>interleaveAll(Collections.emptyList(), 1, false, 10)
                                    .runToList();

        // then
        assertEquals(Collections.emptyList(), actual);
    }

    @Test
    void shouldInterleaveSingleFlow() throws Exception {
        // given
        Flow<Integer> c = Flows.fromValues(1, 2, 3);

        // when
        List<Integer> result = Flows.interleaveAll(List.of(c), 1, false, 10)
                                    .runToList();

        // then
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void shouldInterleaveMultipleFlows() throws Exception {
        // given
        Flow<Integer> c1 = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8);
        Flow<Integer> c2 = Flows.fromValues(10, 20, 30);
        Flow<Integer> c3 = Flows.fromValues(100, 200, 300, 400, 500);

        // when
        List<Integer> result = Flows.interleaveAll(List.of(c1, c2, c3), 1, false, 10)
                                    .runToList();

        // then
        assertEquals(List.of(1, 10, 100, 2, 20, 200, 3, 30, 300, 4, 400, 5, 500, 6, 7, 8), result);
    }

    @Test
    void shouldInterleaveMultipleFlowsUsingSegmentSizeBiggerThan1() throws Exception {
        // given
        Flow<Integer> c1 = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8);
        Flow<Integer> c2 = Flows.fromValues(10, 20, 30);
        Flow<Integer> c3 = Flows.fromValues(100, 200, 300, 400, 500);

        // when
        List<Integer> result = Flows.interleaveAll(List.of(c1, c2, c3), 2, false, 10)
                                    .runToList();

        // then
        assertEquals(List.of(1, 2, 10, 20, 100, 200, 3, 4, 30, 300, 400, 5, 6, 500, 7, 8), result);
    }

    @Test
    void shouldInterleaveMultipleFlowsUsingSegmentSizeBiggerThan1AndCompleteEagerly() throws Exception {
        // given
        Flow<Integer> c1 = Flows.fromValues(1, 2, 3, 4, 5, 6, 7, 8);
        Flow<Integer> c2 = Flows.fromValues(10, 20, 30);
        Flow<Integer> c3 = Flows.fromValues(100, 200, 300, 400, 500);

        // when
        List<Integer> result = Flows.interleaveAll(List.of(c1, c2, c3), 2, true, 10).runToList();

        // then
        assertEquals(List.of(1, 2, 10, 20, 100, 200, 3, 4, 30), result);
    }

    @Test
    void handleEmptyInputStream() throws Exception {
        assertEquals(List.of(), Flows.fromInputStream(emptyInputStream(), 1024).runToList());
    }

    @Test
    void handleInputStreamShorterThanBufferSize() throws Exception {
        assertEquals(List.of("abc"), toStrings(Flows.fromInputStream(inputStream("abc", false), 1024)));
    }

    @Test
    void handleInputStreamLongerThanBufferSize() throws Exception {
        assertEquals(List.of("som", "e t", "ext"), toStrings(Flows.fromInputStream(inputStream("some text", false), 3)));
    }

    @Test
    void closeInputStreamAfterReadingIt() throws Exception {
        TestInputStream is = inputStream("abc", false);
        assertFalse(is.isClosed());
        Flows.fromInputStream(is, 1024).runToList();
        assertTrue(is.isClosed());
    }

    @Test
    void closeInputStreamAfterFailingWithException() {
        TestInputStream is = inputStream("abc", true);
        assertFalse(is.isClosed());
        assertThrows(Exception.class, () -> Flows.fromInputStream(is, 1024).runToList());
        assertTrue(is.isClosed());
    }

    @Test
    void readContentFromFileSmallerThanChunkSize() throws Exception {
        Path path = Files.createTempFile("ox", "test-readfile1");
        Files.write(path, "Test1 file content".getBytes());
        try {
            List<String> result = toStrings(Flows.fromFile(path, 1024));
            assertEquals(List.of("Test1 file content"), result);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void readContentFromFileLargerThanChunkSize() throws Exception {
        Path path = Files.createTempFile("ox", "test-readfile1");
        Files.write(path, "Test2 file content".getBytes());
        try {
            List<String> result = toStrings(Flows.fromFile(path, 3));
            assertEquals(List.of("Tes", "t2 ", "fil", "e c", "ont", "ent"), result);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void handleEmptyFile() throws Exception {
        Path path = Files.createTempFile("ox", "test-readfile1");
        try {
            List<String> result = toStrings(Flows.fromFile(path, 1024));
            assertEquals(List.of(), result);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void throwExceptionForMissingFile() {
        Path path = Paths.get("/no/such/file.txt");
        assertThrows(NoSuchFileException.class, () -> Flows.fromFile(path, 1024).runToList());
    }

    @Test
    void throwExceptionIfPathIsDirectory() throws URISyntaxException {
        Path path = Paths.get(getClass().getResource("/").toURI());
        IOException exception = assertThrows(IOException.class, () -> Flows.fromFile(path, 1024).runToList());
        assertTrue(exception.getMessage().endsWith("is a directory"));
    }

    @Test
    void shouldUnfoldFunction() throws Exception {
        Flow<Integer> c = Flows.unfold(0, i -> i < 3 ? Optional.of(Map.entry(i, i + 1)) : Optional.empty());
        assertEquals(List.of(0, 1, 2), c.runToList());
    }

    @Test
    void shouldRunFromIteratorOnlyOnce() throws Exception {
        Flow<Integer> flow = Flows.fromIterator(List.of(1, 2, 3).iterator());

        assertEquals(List.of(1, 2, 3), flow.runToList()); // first run traverses iterator
        assertEquals(Collections.emptyList(), flow.runToList()); // second run is empty, as iterator is exhausted
    }

    @Test
    void shouldRunFromIteratorSupplierMultipleTimes() throws Exception {
        List<Integer> source = List.of(1, 2, 3);
        var flow = Flows.fromIterator(source::iterator);

        for (int i = 0; i < 5; i++) {
            assertEquals(List.of(1, 2, 3), flow.runToList()); // each run gets new iterator, and is able to traverse it
        }
    }

    private List<String> toStrings(Flow<ByteChunk> source) throws Exception {
        return source.runToList().stream()
                     .map(chunk -> chunk.convertToString(StandardCharsets.UTF_8))
                     .toList();
    }

    private TestInputStream emptyInputStream() {
        return new TestInputStream("");
    }

    private TestInputStream inputStream(String text, boolean failing) {
        return new TestInputStream(text, failing);
    }

    private static class TestInputStream extends ByteArrayInputStream {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final boolean throwOnRead;

        public TestInputStream(String text) {
            this(text, false);
        }

        public TestInputStream(String text, boolean throwOnRead) {
            super(text.getBytes());
            this.throwOnRead = throwOnRead;
        }

        @Override
        public void close() throws IOException {
            closed.set(true);
            super.close();
        }

        @Override
        public int read(byte[] a) throws IOException {
            if (throwOnRead) {
                throw new IOException("expected failed read");
            } else {
                return super.read(a);
            }
        }

        public boolean isClosed() {
            return closed.get();
        }
    }
}
