package org.example.reader.query;

import org.example.engine.TradeQueryingService;
import org.example.shared.ConvertedTrade;
import org.example.shared.interfaces.Worker;

import java.util.ArrayDeque;
import java.util.Queue;

public final class TradeQueryInserter implements Worker {

    private final TradeQueryingService tradeQueryingService;
    private final Queue<ConvertedTrade> trades;

    public TradeQueryInserter(final TradeQueryingService tradeQueryingService) {
        this.tradeQueryingService = tradeQueryingService;
        this.trades = new ArrayDeque<>(100000);
    }

    public void addTrade(final ConvertedTrade trade) {
        trades.offer(trade);
    }

    @Override
    public void doWork() {
        for (int i = 0; i < trades.size(); i++) {
            final ConvertedTrade trade = trades.poll();
            tradeQueryingService.makeTradeQueryable(trade);
        }
    }
}
