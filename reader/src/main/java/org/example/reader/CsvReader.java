package org.example.reader;

import org.example.engine.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class CsvReader {

    private static final Logger LOG = LoggerFactory.getLogger(CsvReader.class);
    private static final int BATCH_SIZE = 1000;
    private static final int MAX_STRING_LENGTH = 256;

    public static void readCsvFile(final String filePath, final UserStore userStore)
    {
        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        try (final FileChannel fileChannel = new RandomAccessFile(Paths.get(filePath).toAbsolutePath().toString(), "r")
                .getChannel()) {
            final MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                    fileChannel.size());
            final StringBuilder line = new StringBuilder(MAX_STRING_LENGTH);
            boolean skipFirstLine = true;
            final List<String> batch = new ArrayList<>(BATCH_SIZE);
            for (int i = 0; i < fileChannel.size(); i++) {
                final char character = (char) buffer.get();

                if (character == '\n' || character == '\r') {
                    if (character == '\r' && buffer.get(i + 1) == '\n') {
                        i++;
                    }
                    if (!line.isEmpty() && skipFirstLine) {
                        skipFirstLine = false;
                        line.setLength(0);
                        continue;
                    }

                    if (!line.isEmpty()) {
                        batch.add(line.toString());
                        if (batch.size() > BATCH_SIZE) {
                            submitBatch(batch, userStore, futures, executorService);
                        }
                        line.setLength(0);
                    }
                } else {
                    line.append(character);
                }
            }
            if (!line.isEmpty()) {
                batch.add(line.toString());
            }

            if (!batch.isEmpty()) {
                submitBatch(batch, userStore, futures, executorService);
            }
        } catch (final Exception e) {
            LOG.error("error when trying to read file: ", e);
        } finally {
            final CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.join();

            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    private static void submitBatch(final List<String> batch,
                                    final UserStore userStore,
                                    final List<CompletableFuture<?>> futures,
                                    final ExecutorService executorService) {
        final List<String> currentBatch = new ArrayList<>(batch);
        batch.clear();

        final CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
            for (final String line : currentBatch) {
                LineProcessor.processLine(line, userStore);
            }
        }, executorService);

        futures.add(future);
    }
}
