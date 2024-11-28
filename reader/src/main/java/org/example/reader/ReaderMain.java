package org.example.reader;

import org.example.engine.TradeQueryingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;

final class ReaderMain {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderMain.class);

    public static void main(final String[] args) {
        LOG.info("starting reader");
        if (args.length < 1) {
            LOG.warn("no file path provided, exiting now");
            return;
        }
        final String filePath = args[0];
        if (LOG.isDebugEnabled()) {
            LOG.debug("file path is {}", filePath);
        }

        long elapsed = 0;
        final long start = Clock.systemUTC().millis();
        final TradeQueryingService tradeQueryingService = new TradeQueryingService();
        final TradeIndexingService tradeIndexingService = new TradeIndexingService(tradeQueryingService);
        final CsvReader reader = new CsvReader(tradeIndexingService);
        reader.readFile(filePath);
        tradeIndexingService.finishProcessingTrades();
        tradeIndexingService.makeTradesQueryable();
        final long end = Clock.systemUTC().millis();
        elapsed += end - start;
        LOG.info("elapsed time is {}ms", elapsed);
    }
}
