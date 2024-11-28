package org.example.reader;

import org.example.engine.TradeQueryingService;
import org.example.shared.ConvertedTrade;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TradeIndexingService {

    private final ConcurrentMap<String, ConvertedTrade> newTrades;
    private final ConcurrentMap<String, ConvertedTrade> amendedTrades;
    private final ConcurrentMap<String, ConvertedTrade> cancelledTrades;
    private final TradeQueryingService tradeQueryingService;

    public TradeIndexingService(final TradeQueryingService tradeQueryingService) {
        final int numberOfProcessors = Runtime.getRuntime().availableProcessors();
        this.newTrades = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.amendedTrades = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.cancelledTrades = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradeQueryingService = tradeQueryingService;
    }

    public void indexTrade(final ConvertedTrade trade) {
        switch (trade.action()) {
            case NEW -> newTrades.put(trade.tradeId(), trade);
            case AMEND -> amendedTrades.put(trade.tradeId(), trade);
            case CANCEL -> cancelledTrades.put(trade.tradeId(), trade);
        }
    }

    public void finishProcessingTrades() {
        for (final Map.Entry<String, ConvertedTrade> entry : cancelledTrades.entrySet()) {
            final String tradeId = entry.getKey();
            newTrades.remove(tradeId);
            amendedTrades.remove(tradeId);
        }
        cancelledTrades.clear();
        for (final Map.Entry<String, ConvertedTrade> entry : amendedTrades.entrySet()) {
            final String tradeId = entry.getKey();
            newTrades.remove(tradeId);
        }
    }

    public void makeTradesQueryable() {
        for (final ConvertedTrade trade : amendedTrades.values()) {
            tradeQueryingService.makeTradeQueryable(trade);
        }
        for (final ConvertedTrade trade : newTrades.values()) {
            tradeQueryingService.makeTradeQueryable(trade);
        }
    }

}
