package org.example.reader;

import org.example.engine.UserStore;
import org.example.shared.Action;
import org.example.shared.Currency;
import org.example.shared.Side;
import org.example.shared.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

final class LineProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LineProcessor.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static void processLine(final String lineToProcess, final UserStore userStore) {
        final String[] lineParts = lineToProcess.split(",", 13);
        if (lineParts.length != 13) {
            LOG.warn("Invalid line format: {}", lineToProcess);
            return;
        }

        try {
            final String tradeId = lineParts[0];
            final String bbgCode = lineParts[1];
            final Currency currency = Currency.from(lineParts[2]);
            final Side side = Side.from(lineParts[3]);
            final double price = Double.parseDouble(lineParts[4]);
            final int volume = Integer.parseInt(lineParts[5]);
            final String portfolio = lineParts[6];
            final Action action = Action.from(lineParts[7]);
            final String account = lineParts[8];
            final String strategy = lineParts[9];
            final String user = lineParts[10];
            final LocalDateTime dateTime = LocalDateTime.parse(lineParts[11], DATE_TIME_FORMATTER);
            final long timeInMs = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();

            final Trade trade = new Trade(tradeId, currency, side, price, volume, action, timeInMs);
            userStore.registerTrade(trade, user, account, portfolio, strategy, bbgCode);
        } catch (NumberFormatException | DateTimeParseException e) {
            LOG.warn("Unable to convert fields in line to object: {} {}", lineToProcess, e.getMessage());
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid value in line fields: {} {}", lineToProcess, e.getMessage());
        }
    }
}
