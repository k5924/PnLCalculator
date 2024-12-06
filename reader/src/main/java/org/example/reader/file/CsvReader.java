package org.example.reader.file;

import org.example.reader.index.Indexer;
import org.example.shared.DefaultWorkerPool;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public final class CsvReader {

    private final DefaultWorkerPool<Indexer> workerPool;
    private final int numberOfProcessors;

    public CsvReader(final DefaultWorkerPool<Indexer> workerPool,
                     final int numberOfProcessors) {
        this.workerPool = workerPool;
        this.numberOfProcessors = numberOfProcessors;
    }

    public void readFile(final String path) {
        try (RandomAccessFile raf = new RandomAccessFile(path, "r");
             FileChannel fc = raf.getChannel()) {

            final MappedByteBuffer initialBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            final int endOfFirstLine = findPositionAfterNewLine(initialBuffer);
            final int adjustedLength = initialBuffer.limit() - endOfFirstLine;
            final int averageSize = adjustedLength % numberOfProcessors == 0 ? adjustedLength / numberOfProcessors : (adjustedLength + 1) / numberOfProcessors;
            int startPos = endOfFirstLine;
            for (int i = 0; i < numberOfProcessors - 1; i++) {
                final int positionToSearchFrom = averageSize + startPos;
                final int positionBeforeNewLine = findPositionBeforeNewLine(initialBuffer, positionToSearchFrom);
                final int positionToSliceAt = positionToSearchFrom + positionBeforeNewLine;
                final int length = positionToSliceAt - startPos;
                final Indexer indexer = workerPool.get();
                indexer.clear();
                indexer.setData(initialBuffer, startPos, length);
                startPos += length + 3;
            }
            final int length = initialBuffer.limit() - startPos;
            final Indexer indexer = workerPool.get();
            indexer.clear();
            indexer.setData(initialBuffer, startPos, length);

            workerPool.doWork();

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static int findPositionBeforeNewLine(final MappedByteBuffer buffer, final int offset) {
        int positionOfBeforeNewLine = 0;
        for (int i = 0; i < buffer.limit() - offset; i++) {
            if (buffer.get(i + offset) == '\r') {
                positionOfBeforeNewLine = i - 1;
                break;
            }
        }
        return positionOfBeforeNewLine;
    }

    private static int findPositionAfterNewLine(final MappedByteBuffer buffer) {
        int positionAfterNewLine = 0;
        for (int i = 0; i < buffer.limit(); i++) {
            if (buffer.get(i) == '\r') {
                positionAfterNewLine = i + 2;
                break;
            }
        }
        return positionAfterNewLine;
    }
}
