package org.example.reader;

import org.example.shared.Action;
import org.example.shared.ConvertedTrade;
import org.example.shared.Currency;
import org.example.shared.Side;

import java.math.BigDecimal;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class Trade {

    private MappedByteBuffer slice;
    private int offset;
    private int[] startPositions;
    private int[] wordLengths;

    public void setData(final MappedByteBuffer slice, final int offset, final int[] startPositions, final int[] wordLengths) {
        this.slice = slice;
        this.offset = offset;
        this.startPositions = startPositions;
        this.wordLengths = wordLengths;
    }

    public ConvertedTrade convert() {
        final String tradeId = extractString(0);
        final String bggCode = extractString(1);
        final Currency currency = extractCurrency(2);
        final Side side = extractSide(3);
        final BigDecimal price = extractPrice(4);
        final int volume = extractVolume(5);
        final String portfolio = extractString(6);
        final Action action = extractAction(7);
        final String account = extractString(8);
        final String strategy = extractString(9);
        final String user = extractString(10);
        final String tradeTime = extractString(11);
        final String valueDate = extractString(12);
        Arrays.fill(startPositions, 0);
        Arrays.fill(wordLengths, 0);
        return new ConvertedTrade(tradeId, bggCode, currency, side, price, volume, portfolio, action, account,
                strategy, user, tradeTime, valueDate);
    }

    private String extractString(final int index) {
        final int length = wordLengths[index];
        final int wordStartPosition = startPositions[index] + offset;
        final byte[] temp = new byte[length];
        slice.get(wordStartPosition, temp, 0, length);
        return new String(temp, StandardCharsets.US_ASCII);
    }

    private Currency extractCurrency(final int index) {
        final int length = wordLengths[index];
        final int wordStartPosition = startPositions[index] + offset;
        final byte[] temp = new byte[length];
        slice.get(wordStartPosition, temp, 0, length);
        for (final Currency currency : Currency.values()) {
            if (currency.matches(temp)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unknown currency encoding in buffer");
    }

    private Side extractSide(final int index) {
        final int wordStartPosition = startPositions[index] + offset;
        final byte c = slice.get(wordStartPosition);
        for (final Side side : Side.values()) {
            if (side.matches(c)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown side encoding in buffer");
    }

    private BigDecimal extractPrice(final int index) {
        final int length = wordLengths[index];
        final int wordStartPosition = startPositions[index] + offset;
        final byte[] temp = new byte[length];
        slice.get(wordStartPosition, temp, 0, length);
        return new BigDecimal(new String(temp, StandardCharsets.US_ASCII));
    }

    private int extractVolume(final int index) {
        final int length = wordLengths[index];
        final int wordStartPosition = startPositions[index] + offset;
        final byte[] temp = new byte[length];
        slice.get(wordStartPosition, temp, 0, length);
        return Integer.parseInt(new String(temp, StandardCharsets.US_ASCII));
    }

    private Action extractAction(final int index) {
        final int length = wordLengths[index];
        final int wordStartPosition = startPositions[index] + offset;
        final byte[] temp = new byte[length];
        slice.get(wordStartPosition, temp, 0, length);
        for (final Action action : Action.values()) {
            if (action.matches(temp)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action encoding in buffer");
    }
}
