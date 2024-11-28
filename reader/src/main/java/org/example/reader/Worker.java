package org.example.reader;

import org.example.shared.ConvertedTrade;

import java.nio.MappedByteBuffer;

public final class Worker {

    private final TradeIndexingService tradeIndexingService;
    private MappedByteBuffer slice;

    public Worker(final TradeIndexingService tradeIndexingService) {
        this.tradeIndexingService = tradeIndexingService;
    }

    public void setSlice(final MappedByteBuffer slice) {
        this.slice = slice;
    }

    public void processSlice() {
        int startOfWord = 0;
        final Trade trade = new Trade();
        for (int i = 0; i < slice.limit(); i++) {
            final char c = (char) slice.get();
            if (c == '\r') {
                final int length = i - startOfWord;
                final MappedByteBuffer word = slice.slice(startOfWord, length);
                trade.provideSlice(word);
                final ConvertedTrade convertedTrade = trade.convert();
                startOfWord = i + 2;
                tradeIndexingService.indexTrade(convertedTrade);
            }
            if (c == ',') {
                final int length = i - startOfWord;
                final MappedByteBuffer word = slice.slice(startOfWord, length);
                trade.provideSlice(word);
                startOfWord = i + 1;
            }
        }
        final int length = slice.limit() - startOfWord;
        final MappedByteBuffer word = slice.slice(startOfWord, length);
        trade.provideSlice(word);
        final ConvertedTrade convertedTrade = trade.convert();
        tradeIndexingService.indexTrade(convertedTrade);
    }
}
