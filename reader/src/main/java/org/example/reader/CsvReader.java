package org.example.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public final class CsvReader {

    private static final Logger LOG = LoggerFactory.getLogger(CsvReader.class);

    public static void readFile(final String path) {
        try (RandomAccessFile raf = new RandomAccessFile(path, "r");
             FileChannel fc = raf.getChannel()) {

            final MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            final int[] wordPositions = new int[13];
            int startOfLine = 0;
            int currentWordPos = 0;
            boolean shouldProcess = false;
            for (int i = 0; i < buffer.limit(); i++) {
                final byte b = buffer.get();
                if (b == '\n') {
                    if (shouldProcess) {
                        final int endOfLine = i - 1;
                        final int length = endOfLine - startOfLine;
                        final MappedByteBuffer bufferSlice = buffer.slice(startOfLine, length);
                        LineProcessor.process(bufferSlice, wordPositions);
                    }
                    Arrays.fill(wordPositions, 0);
                    currentWordPos = 0;
                    startOfLine = i + 1;
                    wordPositions[currentWordPos++] = (i + 1) - startOfLine;
                    shouldProcess = true;
                }
                if (b == ',' && shouldProcess) {
                    wordPositions[currentWordPos++] = (i + 1) - startOfLine;
                }
            }
            final int endOfLine = buffer.limit();
            final int length = endOfLine - startOfLine;
            final MappedByteBuffer bufferSlice = buffer.slice(startOfLine, length);
            LineProcessor.process(bufferSlice, wordPositions);
            Arrays.fill(wordPositions, 0);

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
