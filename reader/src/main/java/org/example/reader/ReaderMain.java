package org.example.reader;

import org.example.engine.TradeQueryingService;
import org.example.reader.file.CsvReader;
import org.example.reader.index.LineProcessor;
import org.example.reader.index.TradeIndexSharder;
import org.example.reader.index.TradeIndexingService;
import org.example.shared.DefaultWorkerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        final int numberOfProcessors = Runtime.getRuntime().availableProcessors();
        final TradeQueryingService tradeQueryingService = new TradeQueryingService(numberOfProcessors);
        final TradeIndexingService tradeIndexingService = new TradeIndexingService(tradeQueryingService, numberOfProcessors);
        final TradeIndexSharder tradeIndexSharder = new TradeIndexSharder(tradeIndexingService, numberOfProcessors);
        final DefaultWorkerPool<LineProcessor> lineProcessingPool = new DefaultWorkerPool<>(() -> new LineProcessor(tradeIndexSharder), numberOfProcessors)
                .setNumberOfWorkers(numberOfProcessors);
        final CsvReader reader = new CsvReader(lineProcessingPool, numberOfProcessors);
        final int numberOfRuns = 100;
        final Observer overallObserver = new Observer("total run", numberOfRuns);
        final Observer readerObserver = new Observer("reading file", numberOfRuns);
        final Observer lineProcessingObserver = new Observer("processing lines", numberOfRuns);
        final Observer shardTradesObserver = new Observer("shard trades", numberOfRuns);
        final Observer tradeCleaner = new Observer("clean trades", numberOfRuns);
        final Observer makeTradeQueryable = new Observer("make queryable", numberOfRuns);
        final Observer clearSharder = new Observer("clear sharder", numberOfRuns);
        final Observer clearIndexer = new Observer("clear indexer", numberOfRuns);
        final Observer clearQueryer = new Observer("clear queryer", numberOfRuns);
        for (int i = 0; i < numberOfRuns; i++) {
            overallObserver.observe(() -> {
                readerObserver.observe(() -> reader.readFile(filePath));
                lineProcessingObserver.observe(lineProcessingPool::doWork);
                shardTradesObserver.observe(tradeIndexSharder::doWork);
                tradeCleaner.observe(tradeIndexingService::finishProcessingTrades);
                makeTradeQueryable.observe(tradeIndexingService::makeTradesQueryable);
                clearSharder.observe(tradeIndexSharder::clear);
                clearIndexer.observe(tradeIndexingService::clear);
                clearQueryer.observe(tradeQueryingService::clear);
            });
        }
        LOG.info("{}", overallObserver);
        LOG.info("{}", readerObserver);
        LOG.info("{}", lineProcessingObserver);
        LOG.info("{}", shardTradesObserver);
        LOG.info("{}", tradeCleaner);
        LOG.info("{}", makeTradeQueryable);
        LOG.info("{}", clearSharder);
        LOG.info("{}", clearIndexer);
        LOG.info("{}", clearQueryer);
    }
}
