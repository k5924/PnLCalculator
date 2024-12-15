package org.example.reader.index;

import org.example.shared.ConvertedTrade;
import org.example.shared.interfaces.Reusable;
import org.example.shared.interfaces.Worker;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentMap;

public final class TradeCleaner implements Reusable, Worker {

    private final ConcurrentMap<String, ConvertedTrade> mapToRemoveFrom;
    private final ArrayDeque<String> tradeIds;

    public TradeCleaner(final ConcurrentMap<String, ConvertedTrade> mapToRemoveFrom) {
        this.tradeIds = new ArrayDeque<>(100000);
        this.mapToRemoveFrom = mapToRemoveFrom;
    }

    public void addId(final String id) {
        this.tradeIds.add(id);
    }

    @Override
    public void doWork() {
        final int size = tradeIds.size();
        for (int i = 0; i < size; i++) {
            final String id = tradeIds.poll();
            if (id == null) {
                break;
            }
            mapToRemoveFrom.remove(id);
        }
    }

    @Override
    public void clear() {
        this.tradeIds.clear();
    }

}
