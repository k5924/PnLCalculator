package org.example.reader.index;

import org.example.shared.ConvertedTrade;
import org.example.shared.DefaultWorkerPool;
import org.example.shared.interfaces.Reusable;
import org.example.shared.interfaces.Worker;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class TradeIndexSharder implements Reusable, Worker {

    private final ConcurrentLinkedDeque<ConvertedTrade> newTrades;
    private final ConcurrentLinkedDeque<ConvertedTrade> amendedTrades;
    private final ConcurrentLinkedDeque<ConvertedTrade> cancelledTrades;
    private final DefaultWorkerPool<TradeIndexer> newTradePool;
    private final DefaultWorkerPool<TradeIndexer> amendedTradePool;
    private final DefaultWorkerPool<TradeIndexer> cancelledTradePool;

    public TradeIndexSharder(final TradeIndexingService tradeIndexingService,
                             final int numberOfProcessors) {
        this.newTrades = new ConcurrentLinkedDeque<>();
        this.amendedTrades = new ConcurrentLinkedDeque<>();
        this.cancelledTrades = new ConcurrentLinkedDeque<>();
        this.newTradePool = new DefaultWorkerPool<>(() -> new TradeIndexer(tradeIndexingService), numberOfProcessors);
        this.amendedTradePool = new DefaultWorkerPool<>(() -> new TradeIndexer(tradeIndexingService), numberOfProcessors);
        this.cancelledTradePool = new DefaultWorkerPool<>(() -> new TradeIndexer(tradeIndexingService), numberOfProcessors);
    }

    public void shardTrade(final ConvertedTrade trade) {
        switch (trade.action()) {
            case NEW -> newTrades.add(trade);
            case AMEND -> amendedTrades.add(trade);
            case CANCEL -> cancelledTrades.add(trade);
        }
    }

    @Override
    public void doWork() {
        newTradePool.setNumberOfWorkers(newTrades.size());
        indexTradesInPool(newTrades, newTradePool);
        amendedTradePool.setNumberOfWorkers(amendedTrades.size());
        indexTradesInPool(amendedTrades, amendedTradePool);
        cancelledTradePool.setNumberOfWorkers(cancelledTrades.size());
        indexTradesInPool(cancelledTrades, cancelledTradePool);
        newTradePool.doWork();
        amendedTradePool.doWork();
        cancelledTradePool.doWork();
    }

    private void indexTradesInPool(final Queue<ConvertedTrade> trades, final DefaultWorkerPool<TradeIndexer> pool) {
        final int queueSize = trades.size();
        for (int i = 0; i < queueSize; i++) {
            final ConvertedTrade trade = trades.poll();
            if (trade == null) {
                break;
            }
            final TradeIndexer tradeIndexer = pool.get(trade.tradeId().hashCode());
            tradeIndexer.addTrade(trade);
        }
    }

    @Override
    public void clear() {
        this.newTrades.clear();
        this.amendedTrades.clear();
        this.cancelledTrades.clear();
    }
}
