package org.example.reader;

import org.example.shared.ConvertedTrade;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TradeIndexingService {

    private final ConcurrentMap<String, ConvertedTrade> newTrades;
    private final ConcurrentMap<String, ConvertedTrade> amendedTrades;
    private final ConcurrentMap<String, ConvertedTrade> cancelledTrades;
    private final TradeInserterPool tradeInserterPool;

    public TradeIndexingService(final TradeInserterPool tradeInserterPool,
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
            tradeInserterPool.submitTrade(trade);
        }
        amendedTrades.clear();
        for (final ConvertedTrade trade : newTrades.values()) {
            tradeInserterPool.submitTrade(trade);
        }
        newTrades.clear();
        tradeInserterPool.doWork();
    }

}
