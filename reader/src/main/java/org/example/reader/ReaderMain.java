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
        final int numberOfProcessors = Runtime.getRuntime().availableProcessors();
        final TradeQueryingService tradeQueryingService = new TradeQueryingService(numberOfProcessors);
        final TradeInserterPool tradeInserterPool = new TradeInserterPool(numberOfProcessors, tradeQueryingService);
        final TradeIndexingService tradeIndexingService = new TradeIndexingService(tradeInserterPool, numberOfProcessors);
        final WorkerPool workerPool = new WorkerPool(tradeIndexingService, numberOfProcessors);
        final CsvReader reader = new CsvReader(workerPool, numberOfProcessors);
        for (int i = 0; i < 100; i++) {
            final long start = Clock.systemUTC().millis();
            reader.readFile(filePath);
            tradeIndexingService.finishProcessingTrades();
            tradeIndexingService.makeTradesQueryable();
            tradeQueryingService.clear();
            final long end = Clock.systemUTC().millis();
            elapsed += end - start;
        }
        LOG.info("average elapsed time over 100 runs is {}ms", elapsed / 100);
    }
}
