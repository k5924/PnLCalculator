package org.example.reader.index;

import org.example.reader.query.TradeQueryInserter;
import org.example.shared.ConvertedTrade;
import org.example.shared.DefaultWorkerPool;
import org.example.shared.interfaces.Reusable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TradeIndexingService implements Reusable {

    private final ConcurrentMap<String, ConvertedTrade> newTrades;
    private final ConcurrentMap<String, ConvertedTrade> amendedTrades;
    private final ConcurrentMap<String, ConvertedTrade> cancelledTrades;
    private final DefaultWorkerPool<TradeQueryInserter> tradeInserterPool;

    public TradeIndexingService(final DefaultWorkerPool<TradeQueryInserter> tradeInserterPool,
                                final int numberOfProcessors) {
        this.newTrades = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.amendedTrades = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.cancelledTrades = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradeInserterPool = tradeInserterPool;
    }

    public void indexTrade(final ConvertedTrade trade) {
        switch (trade.action()) {
            case NEW -> newTrades.put(trade.tradeId(), trade);
            case AMEND -> amendedTrades.put(trade.tradeId(), trade);
            case CANCEL -> cancelledTrades.put(trade.tradeId(), trade);
        }
    }

    public void finishProcessingTrades() {
        for (final String tradeId : cancelledTrades.keySet()) {
            newTrades.remove(tradeId);
            amendedTrades.remove(tradeId);
        }
        cancelledTrades.clear();
        for (final String tradeId : amendedTrades.keySet()) {
            newTrades.remove(tradeId);
        }
    }

    public void makeTradesQueryable() {
        for (final ConvertedTrade trade : amendedTrades.values()) {
            final TradeQueryInserter inserter = tradeInserterPool.get();
            inserter.addTrade(trade);
        }
        amendedTrades.clear();
        for (final ConvertedTrade trade : newTrades.values()) {
            final TradeQueryInserter inserter = tradeInserterPool.get();
            inserter.addTrade(trade);
        }
        newTrades.clear();
        tradeInserterPool.doWork();
    }

    @Override
    public void clear() {
        this.newTrades.clear();
        this.amendedTrades.clear();
        this.cancelledTrades.clear();
    }
}
