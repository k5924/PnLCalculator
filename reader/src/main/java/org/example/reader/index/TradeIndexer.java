package org.example.reader.index;

import org.example.shared.ConvertedTrade;
import org.example.shared.interfaces.Reusable;
import org.example.shared.interfaces.Worker;

import java.util.ArrayDeque;
import java.util.Queue;

public final class TradeIndexer implements Reusable, Worker {

    private final Queue<ConvertedTrade> trades;
    private final TradeIndexingService tradeIndexingService;

    public TradeIndexer(final TradeIndexingService tradeIndexingService) {
        this.trades = new ArrayDeque<>(100000);
        this.tradeIndexingService = tradeIndexingService;
    }

    public void addTrade(final ConvertedTrade trade) {
        this.trades.add(trade);
    }

    @Override
    public void doWork() {
        final int queueSize = trades.size();
        for (int i = 0; i < queueSize; i++) {
            final ConvertedTrade trade = trades.poll();
            if (trade == null) {
                break;
            }
            tradeIndexingService.indexTrade(trade);
        }
    }

    @Override
    public void clear() {
        this.trades.clear();
    }
}
