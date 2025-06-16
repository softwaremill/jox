package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.softwaremill.jox.structured.Scopes;

public class FlowIOTest {

    @Test
    void returnEmptyInputStreamForEmptySource() throws InterruptedException {
        Scopes.unsupervised(
                scope -> {
                    Flow.ByteFlow source = Flows.<byte[]>empty().toByteFlow();
                    try (InputStream stream = source.runToInputStream(scope)) {
                        assertEquals("", inputStreamToString(stream));
                    }
                    return null;
                });
    }

    @Test
    void returnInputStreamForSimpleSource() throws InterruptedException {
        Scopes.unsupervised(
                scope -> {
                    var source = Flows.fromByteArrays("chunk1".getBytes(), "chunk2".getBytes());
                    try (InputStream stream = source.runToInputStream(scope)) {
                        assertEquals("chunk1chunk2", inputStreamToString(stream));
                    }
                    return null;
                });
    }

    @Test
    void correctlyTrackAvailableBytes() throws InterruptedException {
        Scopes.unsupervised(
                scope -> {
                    var source = Flows.fromByteArrays("chunk1".getBytes(), "chunk2".getBytes());
                    try (InputStream stream = source.runToInputStream(scope)) {
                        assertEquals(0, stream.available());
                        stream.read();
                        assertEquals(5, stream.available());
                        stream.readNBytes(5);
                        assertEquals(0, stream.available());
                        stream.read();
                        stream.read();
                        assertEquals(4, stream.available());
                        stream.readNBytes(5);
                        assertEquals(0, stream.available());
                    }
                    return null;
                });
    }

    @Test
    void runToOutputStream_writeSingleChunkToOutputStream() throws Exception {
        // given
        TestOutputStream outputStream = TestOutputStream.doNotThrowOnWrite();
        String sourceContent = "source.toOutputStream test1 content";
        var source = Flows.fromByteArrays(sourceContent.getBytes());
        assertFalse(outputStream.isClosed());

        // when
        source.runToOutputStream(outputStream);

        // then
        assertEquals(sourceContent, outputStream.toString());
        assertTrue(outputStream.isClosed());
    }

    @Test
    void runToOutputStream_writeMultipleChunksToOutputStream() throws Exception {
        // given
        TestOutputStream outputStream = TestOutputStream.doNotThrowOnWrite();
        String sourceContent = "source.toOutputStream test2 content";
        var source = Flows.fromByteArrays(sourceContent.getBytes());
        assertFalse(outputStream.isClosed());

        // when
        source.runToOutputStream(outputStream);

        // then
        assertEquals(sourceContent, outputStream.toString());
        assertTrue(outputStream.isClosed());
    }

    @Test
    void runToOutputStream_handleEmptySource() throws Exception {
        // given
        TestOutputStream outputStream = TestOutputStream.doNotThrowOnWrite();
        Flow.ByteFlow source = Flows.<byte[]>empty().toByteFlow();

        // when
        source.runToOutputStream(outputStream);

        // then
        assertEquals("", outputStream.toString());
        assertTrue(outputStream.isClosed());
    }

    @Test
    void runToOutputStream_closeOutputStreamOnWriteError() {
        // given
        TestOutputStream outputStream = TestOutputStream.throwOnWrite();
        String sourceContent = "source.toOutputStream test3 content";
        var source = Flows.fromByteArrays(sourceContent.getBytes());
        assertFalse(outputStream.isClosed());

        // when & then
        Exception exception =
                assertThrows(Exception.class, () -> source.runToOutputStream(outputStream));
        assertTrue(outputStream.isClosed());
        assertEquals("expected failed write", exception.getMessage());
    }

    @Test
    void runToOutputStream_closeOutputStreamOnError() {
        // given
        TestOutputStream outputStream = TestOutputStream.doNotThrowOnWrite();
        var source =
                Flows.fromValues("initial content".getBytes())
                        .concat(Flows.failed(new Exception("expected source error")))
                        .toByteFlow();
        assertFalse(outputStream.isClosed());

        // when & then
        Exception exception =
                assertThrows(Exception.class, () -> source.runToOutputStream(outputStream));
        assertTrue(outputStream.isClosed());
        assertEquals("expected source error", exception.getMessage());
    }

    @Test
    void runToFile_createFileAndWriteSingleChunkWithBytes() throws Exception {
        // given
        Path path = Files.createTempFile("jox", "test-writefile1");
        try {
            byte[] sourceContent = "source.toFile test1 content".getBytes();
            var source = Flows.fromByteArrays(sourceContent);

            // when
            source.runToFile(path);

            // then
            assertEquals(List.of(new String(sourceContent)), fileContent(path));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void runToFile_createFileAndWriteMultipleChunksWithBytes() throws Exception {
        // given
        Path path = Files.createTempFile("jox", "test-writefile2");
        try {
            String sourceContent = "source.toFile test2 content";
            var source =
                    Flows.fromIterable(
                                    sourceContent
                                            .chars()
                                            .mapToObj(c -> (byte) c)
                                            .collect(Collectors.groupingBy(equalSizeChunks(4)))
                                            .values())
                            .toByteFlow(FlowIOTest::convertToByteArray);

            // when
            source.runToFile(path);

            // then
            assertEquals(List.of(sourceContent), fileContent(path));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void runToFile_useExistingFileAndOverwriteWithSingleChunkWithBytes() throws Exception {
        // given
        Path path = Files.createTempFile("jox", "test-writefile3");
        try {
            Files.write(path, "Some initial content".getBytes());
            byte[] sourceContent = "source.toFile test3 content".getBytes();
            var source = Flows.fromByteArrays(sourceContent);

            // when
            source.runToFile(path);

            // then
            assertEquals(List.of(new String(sourceContent)), fileContent(path));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void runToFile_handleEmptySource() throws Exception {
        // given
        Path path = Files.createTempFile("jox", "test-writefile4");
        try {
            var source = Flows.fromByteArrays();

            // when
            source.runToFile(path);

            // then
            assertEquals(List.of(), fileContent(path));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void runToFile_throwExceptionOnFailingSource() throws IOException {
        // given
        Path path = Files.createTempFile("jox", "test-writefile5");
        try {
            var source =
                    Flows.fromValues("initial content".getBytes())
                            .concat(Flows.failed(new Exception("expected source error")))
                            .toByteFlow();

            // when & then
            Exception exception = assertThrows(Exception.class, () -> source.runToFile(path));
            assertEquals("expected source error", exception.getMessage());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void runToFile_throwExceptionIfPathIsDirectory() throws URISyntaxException {
        // given
        Path path = Paths.get(getClass().getResource("/").toURI());
        var source = Flows.fromByteArrays(new byte[0]);

        // when & then
        IOException exception = assertThrows(IOException.class, () -> source.runToFile(path));
        assertTrue(exception.getMessage().endsWith("is a directory"));
    }

    @Test
    void runToFile_throwExceptionIfFileCannotBeOpened() {
        // given
        Path folder = Paths.get("/", "not-existing-directory", "not-existing-file");
        Path path = folder.resolve("not-existing-file.txt");
        var source = Flows.fromByteArrays(new byte[0]);

        // when & then
        assertThrows(NoSuchFileException.class, () -> source.runToFile(path));
    }

    @Test
    void runToFile_shouldCreateFile() throws Exception {
        // given
        Path folder = Files.createTempDirectory("jox");
        Path path = folder.resolve("test-file.txt");

        try {
            byte[] sourceContent = "source.toFile test1 content".getBytes();
            var source = Flows.fromByteArrays(sourceContent);

            // when
            source.runToFile(path);

            // then
            assertEquals(List.of(new String(sourceContent)), fileContent(path));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    private List<String> fileContent(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    private static byte[] convertToByteArray(List<Byte> bytes) {
        byte[] buffer = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            buffer[i] = bytes.get(i);
        }
        return buffer;
    }

    private static Function<Byte, Integer> equalSizeChunks(int size) {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger divider = new AtomicInteger(0);
        return _ -> {
            if (counter.incrementAndGet() == size) {
                counter.set(0);
                return divider.getAndIncrement();
            } else {
                return divider.get();
            }
        };
    }

    private static class TestOutputStream extends ByteArrayOutputStream {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final boolean throwOnWrite;

        public TestOutputStream(boolean throwOnWrite) {
            this.throwOnWrite = throwOnWrite;
        }

        public static TestOutputStream throwOnWrite() {
            return new TestOutputStream(true);
        }

        public static TestOutputStream doNotThrowOnWrite() {
            return new TestOutputStream(false);
        }

        @Override
        public void close() throws IOException {
            closed.set(true);
            super.close();
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            if (throwOnWrite) {
                throw new IOException("expected failed write");
            } else {
                super.write(bytes);
            }
        }

        public boolean isClosed() {
            return closed.get();
        }
    }

    private static String inputStreamToString(InputStream stream) throws IOException {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
