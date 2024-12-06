package org.example.reader;

import org.example.engine.TradeQueryingService;
import org.example.reader.file.CsvReader;
import org.example.reader.index.Indexer;
import org.example.reader.index.TradeIndexingService;
import org.example.reader.query.TradeQueryInserter;
import org.example.shared.DefaultWorkerPool;
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

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long elapsed = 0;
        final int numberOfProcessors = Runtime.getRuntime().availableProcessors();
        final TradeQueryingService tradeQueryingService = new TradeQueryingService(numberOfProcessors);
        final DefaultWorkerPool<TradeQueryInserter> tradeQueryInserterPool = new DefaultWorkerPool<>(numberOfProcessors, () -> new TradeQueryInserter(tradeQueryingService));
        final TradeIndexingService tradeIndexingService = new TradeIndexingService(tradeQueryInserterPool, numberOfProcessors);
        final DefaultWorkerPool<Indexer> indexerPool = new DefaultWorkerPool<>(numberOfProcessors, () -> new Indexer(tradeIndexingService));
        final CsvReader reader = new CsvReader(indexerPool, numberOfProcessors);
        for (int i = 0; i < 100; i++) {
            final long start = Clock.systemUTC().millis();
            reader.readFile(filePath);
            tradeIndexingService.finishProcessingTrades();
            tradeIndexingService.makeTradesQueryable();
            tradeIndexingService.clear();
            tradeQueryingService.clear();
            final long end = Clock.systemUTC().millis();
            final long time = end - start;
            elapsed += time;
            min = Math.min(time, min);
            max = Math.max(time, max);
        }
        LOG.info("average elapsed time over 100 runs is {}ms, min was {}ms, max was {}ms", elapsed / 100, min, max);
    }
}
