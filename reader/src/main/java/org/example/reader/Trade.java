package org.example.reader;

import org.example.shared.Action;
import org.example.shared.ConvertedTrade;
import org.example.shared.Currency;
import org.example.shared.Side;

import java.math.BigDecimal;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;

public final class Trade {

    private int position = 0;
    private final MappedByteBuffer[] slices = new MappedByteBuffer[13];

    public void provideSlice(final MappedByteBuffer slice) {
        slices[position++] = slice;
    }

    public ConvertedTrade convert() {
        final String tradeId = extractString(slices[0]);
        final String bggCode = extractString(slices[1]);
        final Currency currency = extractCurrency(slices[2]);
        final Side side = extractSide(slices[3]);
        final BigDecimal price = extractPrice(slices[4]);
        final int volume = extractVolume(slices[5]);
        final String portfolio = extractString(slices[6]);
        final Action action = extractAction(slices[7]);
        final String account = extractString(slices[8]);
        final String strategy = extractString(slices[9]);
        final String user = extractString(slices[10]);
        final String tradeTime = extractString(slices[11]);
        final String valueDate = extractString(slices[12]);
        position = 0;
        return new ConvertedTrade(tradeId, bggCode, currency, side, price, volume, portfolio, action, account,
                strategy, user, tradeTime, valueDate);
    }

    private static String extractString(final MappedByteBuffer buffer) {
        final byte[] temp = new byte[buffer.limit()];
        buffer.get(temp);
        return new String(temp, StandardCharsets.US_ASCII);
    }

    private static Currency extractCurrency(final MappedByteBuffer buffer) {
        final byte[] temp = new byte[buffer.limit()];
        buffer.get(temp);
        for (final Currency currency : Currency.values()) {
            if (currency.matches(temp)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unknown currency encoding in buffer");
    }

    private static Side extractSide(final MappedByteBuffer buffer) {
        final byte c = buffer.get();
        for (final Side side : Side.values()) {
            if (side.matches(c)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown side encoding in buffer");
    }

    private static BigDecimal extractPrice(final MappedByteBuffer buffer) {
        final byte[] temp = new byte[buffer.limit()];
        buffer.get(temp);
        return new BigDecimal(new String(temp, StandardCharsets.US_ASCII));
    }

    private static int extractVolume(final MappedByteBuffer buffer) {
        final byte[] temp = new byte[buffer.limit()];
        buffer.get(temp);
        return Integer.parseInt(new String(temp, StandardCharsets.US_ASCII));
    }

    private static Action extractAction(final MappedByteBuffer buffer) {
        final byte[] temp = new byte[buffer.limit()];
        buffer.get(temp);
        for (final Action action : Action.values()) {
            if (action.matches(temp)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action encoding in buffer");
    }
}
