package org.example.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.*;

public final class CsvReader {

    private static final Logger LOG = LoggerFactory.getLogger(CsvReader.class);
    private final TradeIndexingService tradeIndexingService;

    public CsvReader(final TradeIndexingService tradeIndexingService) {
        this.tradeIndexingService = tradeIndexingService;
    }

    public void readFile(final String path) {
        try (RandomAccessFile raf = new RandomAccessFile(path, "r");
             FileChannel fc = raf.getChannel()) {

            final MappedByteBuffer initialBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            final int endOfFirstLine = findPositionAfterNewLine(initialBuffer, 0);
            final MappedByteBuffer contentBuffer = initialBuffer.slice(endOfFirstLine, initialBuffer.limit() - endOfFirstLine);
            final int numberOfProcessors = Runtime.getRuntime().availableProcessors();
            final Worker[] workers = new Worker[numberOfProcessors];
            final int averageSize = contentBuffer.limit() % numberOfProcessors == 0 ? contentBuffer.limit() / numberOfProcessors : (contentBuffer.limit() + 1) / numberOfProcessors;
            int startPos = 0;
            for (int i = 0; i < numberOfProcessors - 1; i++) {
                contentBuffer.position(startPos);
                final int positionToSearchFrom = averageSize + startPos;
                final int positionBeforeNewLine = findPositionBeforeNewLine(contentBuffer, positionToSearchFrom);
                final int positionToSliceAt = positionToSearchFrom + positionBeforeNewLine;
                final int length = positionToSliceAt - startPos;
                final MappedByteBuffer slice = contentBuffer.slice(startPos, length);
                final Worker worker = new Worker(tradeIndexingService);
                worker.setSlice(slice);
                workers[i] = worker;
                startPos += length + 3;
            }
            contentBuffer.position(startPos);
            final int length = contentBuffer.limit() - startPos;
            final MappedByteBuffer slice = contentBuffer.slice(startPos, length);
            final Worker worker = new Worker(tradeIndexingService);
            worker.setSlice(slice);
            workers[numberOfProcessors - 1] = worker;

            final ExecutorService executorService = Executors.newFixedThreadPool(numberOfProcessors);
            for (int i = 0; i < numberOfProcessors; i++) {
                final Worker work = workers[i];
                executorService.submit(work::processSlice);
            }
            executorService.shutdown();
            final boolean finished = executorService.awaitTermination(60, TimeUnit.SECONDS);
            if (!finished) {
                executorService.shutdownNow();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static int findPositionBeforeNewLine(final MappedByteBuffer buffer, final int searchingPosition) {
        buffer.position(searchingPosition);
        int positionOfBeforeNewLine = 0;
        for (int i = 0; i < buffer.limit(); i++) {
            if (buffer.get() == '\r') {
                positionOfBeforeNewLine = i - 1;
                break;
            }
        }
        return positionOfBeforeNewLine;
    }

    private static int findPositionAfterNewLine(final MappedByteBuffer buffer, final int searchingPosition) {
        buffer.position(searchingPosition);
        int positionAfterNewLine = 0;
        for (int i = 0; i < buffer.limit(); i++) {
            if (buffer.get() == '\r') {
                positionAfterNewLine = i + 2;
                break;
            }
        }
        return positionAfterNewLine;
    }
}
