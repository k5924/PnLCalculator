package org.example.reader;

import org.example.shared.ConvertedTrade;

import java.nio.MappedByteBuffer;

public final class Worker {

    private final TradeIndexingService tradeIndexingService;
    private final int[] startPositions;
    private final int[] lengths;
    private final Trade trade;
    private MappedByteBuffer slice;
    private int offset = 0;
    private int lengthToSearch = 0;
    private int startOfWord = 0;
    private int currentIndexInArrs = 0;

    public Worker(final TradeIndexingService tradeIndexingService) {
        this.tradeIndexingService = tradeIndexingService;
        this.startPositions = new int[13];
        this.lengths = new int[13];
        this.trade = new Trade();
    }

    public void setData(final MappedByteBuffer slice,
                  final int offset,
                  final int lengthToSearch) {
        this.slice = slice;
        this.offset = offset;
        this.lengthToSearch = lengthToSearch;
        startOfWord = 0;
        currentIndexInArrs = 0;
        trade.setData(slice, offset, startPositions, lengths);
    }

    public void processSlice() {
        for (int i = 0; i < lengthToSearch; i++) {
            final char c = (char) slice.get(i + offset);
            if (c == '\r') {
                startPositions[currentIndexInArrs] = startOfWord;
                final int length = i - startOfWord;
                lengths[currentIndexInArrs] = length;
                final ConvertedTrade convertedTrade = trade.convert();
                startOfWord = i + 2;
                tradeIndexingService.indexTrade(convertedTrade);
                currentIndexInArrs = 0;
            }
            if (c == ',') {
                startPositions[currentIndexInArrs] = startOfWord;
                final int length = i - startOfWord;
                lengths[currentIndexInArrs] = length;
                startOfWord = i + 1;
                currentIndexInArrs++;
            }
        }
        startPositions[currentIndexInArrs] = startOfWord;
        final int length = lengthToSearch - startOfWord;
        lengths[currentIndexInArrs] = length;
        final ConvertedTrade convertedTrade = trade.convert();
        tradeIndexingService.indexTrade(convertedTrade);
    }
}
