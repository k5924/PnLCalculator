package org.example.reader.query;

import org.example.shared.ConvertedTrade;
import org.example.shared.interfaces.Reusable;
import org.example.shared.interfaces.Worker;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public final class TradeQueryInserter implements Reusable, Worker {

    private final Consumer<ConvertedTrade> tradeConsumer;
    private final ArrayDeque<ConvertedTrade> trades;


    public TradeQueryInserter(final Consumer<ConvertedTrade> tradeConsumer) {
        this.tradeConsumer = tradeConsumer;
        this.trades = new ArrayDeque<>(100000);
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
            tradeConsumer.accept(trade);
        }
    }

    @Override
    public void clear() {
        this.trades.clear();
    }

}
