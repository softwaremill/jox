package com.softwaremill.jox.flows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.softwaremill.jox.ChannelErrorException;
import com.softwaremill.jox.structured.Scopes;
import org.junit.jupiter.api.Test;

public class FlowIOTest {

    @Test
    void returnEmptyInputStreamForEmptySource() throws ExecutionException, InterruptedException {
        Scopes.unsupervised(scope -> {
            Flow<byte[]> source = Flows.empty();
            try (InputStream stream = source.runToInputStream(scope)) {
                assertEquals("", inputStreamToString(stream));
            }
            return null;
        });
    }

    @Test
    void returnInputStreamForSimpleSource() throws ExecutionException, InterruptedException {
        Scopes.unsupervised(scope -> {
            Flow<byte[]> source = Flows.fromValues("chunk1".getBytes(), "chunk2".getBytes());
            try (InputStream stream = source.runToInputStream(scope)) {
                assertEquals("chunk1chunk2", inputStreamToString(stream));
            }
            return null;
        });
    }

    @Test
    void correctlyTrackAvailableBytes() throws ExecutionException, InterruptedException {
        Scopes.unsupervised(scope -> {
            Flow<byte[]> source = Flows.fromValues("chunk1".getBytes(), "chunk2".getBytes());
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
    void runToInputStream_shouldThrowWhenRunOnNonByteArrayFlow() throws ExecutionException, InterruptedException {
        Scopes.unsupervised(scope -> {
            ChannelErrorException exception = assertThrows(ChannelErrorException.class, () -> {
                //noinspection resource
                InputStream inputStream = Flows.fromValues(1, 2, 3).runToInputStream(scope);
                // reading the stream triggers the exception as it is written via channel
                inputStream.readAllBytes();
            });
            assertEquals("requirement failed: method can be called only on flow containing byte[]", exception.getCause().getMessage());
            return null;
        });
    }

    @Test
    void runToOutputStream_writeSingleChunkToOutputStream() throws Exception {
        // given
        TestOutputStream outputStream = TestOutputStream.doNotThrowOnWrite();
        String sourceContent = "source.toOutputStream test1 content";
        Flow<byte[]> source = Flows.fromValues(sourceContent.getBytes());
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
        Flow<byte[]> source = Flows.fromValues(sourceContent.getBytes());
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
        Flow<byte[]> source = Flows.empty();

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
        Flow<byte[]> source = Flows.fromValues(sourceContent.getBytes());
        assertFalse(outputStream.isClosed());

        // when & then
        Exception exception = assertThrows(Exception.class, () -> source.runToOutputStream(outputStream));
        assertTrue(outputStream.isClosed());
        assertEquals("expected failed write", exception.getMessage());
    }

    @Test
    void runToOutputStream_closeOutputStreamOnError() {
        // given
        TestOutputStream outputStream = TestOutputStream.doNotThrowOnWrite();
        Flow<byte[]> source = Flows
                .fromValues("initial content".getBytes())
                .concat(Flows.failed(new Exception("expected source error")));
        assertFalse(outputStream.isClosed());

        // when & then
        Exception exception = assertThrows(Exception.class, () -> source.runToOutputStream(outputStream));
        assertTrue(outputStream.isClosed());
        assertEquals("expected source error", exception.getMessage());
    }

    @Test
    void runToFile_runToOutputStream_shouldThrowWhenRunOnNonByteArrayFlow() {
        // given
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Flows.fromValues(1, 2, 3).runToOutputStream(new ByteArrayOutputStream());
        });

        // then
        assertEquals("requirement failed: method can be called only on flow containing byte[]", exception.getMessage());
    }

    @Test
    void runToFile_createFileAndWriteSingleChunkWithBytes() throws Exception {
        // given
        Path path = Files.createTempFile("jox", "test-writefile1");
        try {
            byte[] sourceContent = "source.toFile test1 content".getBytes();
            Flow<byte[]> source = Flows.fromValues(sourceContent);

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
            Flow<byte[]> source = Flows.fromIterable(
                    sourceContent.chars().mapToObj(c -> (byte) c)
                            .collect(Collectors.groupingBy(equalSizeChunks(4)))
                            .values())
                    .map(FlowIOTest::convertToByteArray);

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
            Flow<byte[]> source = Flows.fromValues(sourceContent);

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
            Flow<byte[]> source = Flows.empty();

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
            Flow<byte[]> source = Flows
                    .fromValues("initial content".getBytes())
                    .concat(Flows.failed(new Exception("expected source error")));

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
        Flow<byte[]> source = Flows.fromValues(new byte[0]);

        // when & then
        IOException exception = assertThrows(IOException.class, () -> source.runToFile(path));
        assertTrue(exception.getMessage().endsWith("is a directory"));
    }

    @Test
    void runToFile_throwExceptionIfFileCannotBeOpened() {
        // given
        Path folder = Paths.get("/", "not-existing-directory", "not-existing-file");
        Path path = folder.resolve("not-existing-file.txt");
        Flow<byte[]> source = Flows.fromValues(new byte[0]);

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
            Flow<byte[]> source = Flows.fromValues(sourceContent);

            // when
            source.runToFile(path);

            // then
            assertEquals(List.of(new String(sourceContent)), fileContent(path));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void runToFile_shouldThrowWhenRunOnNonByteArrayFlow() throws IOException {
        Path path = Files.createTempFile("jox", "test-writefile5");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Flows.fromValues(1, 2, 3).runToFile(path);
        });
        assertEquals("requirement failed: method can be called only on flow containing byte[]", exception.getMessage());
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
