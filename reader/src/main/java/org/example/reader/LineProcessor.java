package org.example.reader;

import org.example.shared.Action;
import org.example.shared.Currency;
import org.example.shared.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;

public final class LineProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LineProcessor.class);

    public static void process(final MappedByteBuffer bufferSlice, final int[] wordStartPositions) {
        final String tradeId = extractString(bufferSlice, wordStartPositions[0], wordStartPositions[1] - 1);
        final String bggCode = extractString(bufferSlice, wordStartPositions[1], wordStartPositions[2] - 1);
        final Currency currency = extractCurrency(bufferSlice, wordStartPositions[2], wordStartPositions[3] - 1);
        final Side side = extractSide(bufferSlice, wordStartPositions[3]);
        final BigDecimal price = extractPrice(bufferSlice, wordStartPositions[4], wordStartPositions[5] - 1);
        final int volume = extractVolume(bufferSlice, wordStartPositions[5], wordStartPositions[6] - 1);
        final String portfolio = extractString(bufferSlice, wordStartPositions[6], wordStartPositions[7] - 1);
        final Action action = extractAction(bufferSlice, wordStartPositions[7], wordStartPositions[8] - 1);
        final String account = extractString(bufferSlice, wordStartPositions[8], wordStartPositions[9] - 1);
        final String strategy = extractString(bufferSlice, wordStartPositions[9], wordStartPositions[10] - 1);
        final String user = extractString(bufferSlice, wordStartPositions[10], wordStartPositions[11] - 1);
        final String tradeTime = extractString(bufferSlice, wordStartPositions[11], wordStartPositions[12] - 1);
        final String valueDate = extractString(bufferSlice, wordStartPositions[12], bufferSlice.limit());
    }

    private static String extractString(final MappedByteBuffer buffer, final int start, final int end) {
        final int length = end - start;
        final byte[] temp = new byte[length];
        buffer.position(start);
        buffer.get(temp, 0, length);
        return new String(temp, StandardCharsets.US_ASCII);
    }

    private static Currency extractCurrency(final MappedByteBuffer buffer, final int start, final int end) {
        final int length = end - start;
        final byte[] temp = new byte[length];
        buffer.position(start);
        buffer.get(temp, 0, length);
        for (final Currency currency : Currency.values()) {
            if (currency.matches(temp)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unknown currency encoding in buffer");
    }

    private static Side extractSide(final MappedByteBuffer buffer, final int start) {
        buffer.position(start);
        final byte c = buffer.get();
        for (final Side side : Side.values()) {
            if (side.matches(c)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown side encoding in buffer");
    }

    private static BigDecimal extractPrice(final MappedByteBuffer buffer, final int start, final int end) {
        final int length = end - start;
        final byte[] temp = new byte[length];
        buffer.position(start);
        buffer.get(temp, 0, length);
        return new BigDecimal(new String(temp, StandardCharsets.US_ASCII));
    }

    private static int extractVolume(final MappedByteBuffer buffer, final int start, final int end) {
        final int length = end - start;
        final byte[] temp = new byte[length];
        buffer.position(start);
        buffer.get(temp, 0, length);
        return Integer.parseInt(new String(temp, StandardCharsets.US_ASCII));
    }

    private static Action extractAction(final MappedByteBuffer buffer, final int start, final int end) {
        final int length = end - start;
        final byte[] temp = new byte[length];
        buffer.position(start);
        buffer.get(temp, 0, length);
        for (final Action action : Action.values()) {
            if (action.matches(temp)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action encoding in buffer");
    }
}
