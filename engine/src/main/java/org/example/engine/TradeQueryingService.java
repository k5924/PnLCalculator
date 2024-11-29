package org.example.engine;

import org.example.shared.Action;
import org.example.shared.ConvertedTrade;
import org.example.shared.Currency;
import org.example.shared.Side;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

public final class TradeQueryingService {

    private final ConcurrentMap<String, ConvertedTrade> tradesById;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByBggCode;
    private final ConcurrentMap<String, BigDecimal> pnlByBggCode;
    private final ConcurrentMap<Currency, ConcurrentLinkedQueue<ConvertedTrade>> tradesByCurrency;
    private final ConcurrentMap<Currency, BigDecimal> pnlByCurrency;
    private final ConcurrentMap<Side, ConcurrentLinkedQueue<ConvertedTrade>> tradesBySide;
    private final ConcurrentMap<Side, BigDecimal> pnlBySide;
    private final ConcurrentMap<BigDecimal, ConcurrentLinkedQueue<ConvertedTrade>> tradesByPrice;
    private final ConcurrentMap<BigDecimal, ConcurrentLinkedQueue<ConvertedTrade>> tradesByPriceSorted;
    private final ConcurrentMap<BigDecimal, BigDecimal> pnlByPrice;
    private final ConcurrentMap<Integer, ConcurrentLinkedQueue<ConvertedTrade>> tradesByVolume;
    private final ConcurrentMap<Integer, ConcurrentLinkedQueue<ConvertedTrade>> tradesByVolumeSorted;
    private final ConcurrentMap<Integer, BigDecimal> pnlByVolume;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByPortfolio;
    private final ConcurrentMap<String, BigDecimal> pnlByPortfolio;
    private final ConcurrentMap<Action, ConcurrentLinkedQueue<ConvertedTrade>> tradesByAction;
    private final ConcurrentMap<Action, BigDecimal> pnlByAction;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByAccount;
    private final ConcurrentMap<String, BigDecimal> pnlByAccount;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByStrategy;
    private final ConcurrentMap<String, BigDecimal> pnlByStrategy;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByUser;
    private final ConcurrentMap<String, BigDecimal> pnlByUser;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByTradeTime;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByTradeTimeSorted;
    private final ConcurrentMap<String, BigDecimal> pnlByTradeTime;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByValueDate;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByValueDateSorted;
    private final ConcurrentMap<String, BigDecimal> pnlByValueDate;


    public TradeQueryingService(final int numberOfProcessors) {
        this.tradesById = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByBggCode = new ConcurrentHashMap<>( 100000, 0.65F, numberOfProcessors);
        this.pnlByBggCode = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByCurrency = new ConcurrentHashMap<>(Currency.values().length, 0.65F, numberOfProcessors);
        this.pnlByCurrency = new ConcurrentHashMap<>(Currency.values().length, 0.65F, numberOfProcessors);
        this.tradesBySide = new ConcurrentHashMap<>(Side.values().length, 0.65F, numberOfProcessors);
        this.pnlBySide = new ConcurrentHashMap<>(Side.values().length, 0.65F, numberOfProcessors);
        this.tradesByPrice = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByPriceSorted = new ConcurrentSkipListMap<>();
        this.pnlByPrice = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByVolume = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByVolumeSorted = new ConcurrentSkipListMap<>();
        this.pnlByVolume = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByPortfolio = new ConcurrentHashMap<>(6, 0.65F, numberOfProcessors);
        this.pnlByPortfolio = new ConcurrentHashMap<>(6, 0.65F, numberOfProcessors);
        this.tradesByAction = new ConcurrentHashMap<>(Action.values().length, 0.65F, numberOfProcessors);
        this.pnlByAction = new ConcurrentHashMap<>(Action.values().length, 0.65F, numberOfProcessors);
        this.tradesByAccount =  new ConcurrentHashMap<>(6, 0.65F, numberOfProcessors);
        this.pnlByAccount =  new ConcurrentHashMap<>(6, 0.65F, numberOfProcessors);
        this.tradesByStrategy =  new ConcurrentHashMap<>(6, 0.65F, numberOfProcessors);
        this.pnlByStrategy =  new ConcurrentHashMap<>(6, 0.65F, numberOfProcessors);
        this.tradesByUser =  new ConcurrentHashMap<>(6, 0.65F, numberOfProcessors);
        this.pnlByUser =  new ConcurrentHashMap<>(6, 0.65F, numberOfProcessors);
        this.tradesByTradeTime = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByTradeTimeSorted =  new ConcurrentSkipListMap<>();
        this.pnlByTradeTime = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByValueDate = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByValueDateSorted =  new ConcurrentSkipListMap<>();
        this.pnlByValueDate = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
    }

    public void makeTradeQueryable(final ConvertedTrade trade) {
        final int multiplier = trade.side().multiplier();
        final double fxToUsd = trade.currency().conversion();
        final double adjustedVolume = trade.volume() * multiplier * fxToUsd;
        final BigDecimal pnl = trade.price().multiply(BigDecimal.valueOf(adjustedVolume));
        tradesById.put(trade.tradeId(), trade);

        tradesByBggCode.computeIfAbsent(trade.bggCode(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByBggCode.put(trade.bggCode(), pnlByBggCode.getOrDefault(trade.bggCode(), BigDecimal.ZERO).add(pnl));

        tradesByCurrency.computeIfAbsent(trade.currency(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByCurrency.put(trade.currency(), pnlByCurrency.getOrDefault(trade.currency(), BigDecimal.ZERO).add(pnl));

        tradesBySide.computeIfAbsent(trade.side(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlBySide.put(trade.side(), pnlBySide.getOrDefault(trade.side(), BigDecimal.ZERO).add(pnl));

        tradesByPrice.computeIfAbsent(trade.price(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        tradesByPriceSorted.computeIfAbsent(trade.price(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByPrice.put(trade.price(), pnlByPrice.getOrDefault(trade.price(), BigDecimal.ZERO).add(pnl));

        tradesByVolume.computeIfAbsent(trade.volume(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        tradesByVolumeSorted.computeIfAbsent(trade.volume(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByVolume.put(trade.volume(), pnlByVolume.getOrDefault(trade.volume(), BigDecimal.ZERO).add(pnl));

        tradesByPortfolio.computeIfAbsent(trade.portfolio(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByPortfolio.put(trade.portfolio(), pnlByPortfolio.getOrDefault(trade.portfolio(), BigDecimal.ZERO).add(pnl));

        tradesByAction.computeIfAbsent(trade.action(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByAction.put(trade.action(), pnlByAction.getOrDefault(trade.action(), BigDecimal.ZERO).add(pnl));

        tradesByAccount.computeIfAbsent(trade.account(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByAccount.put(trade.account(), pnlByAccount.getOrDefault(trade.account(), BigDecimal.ZERO).add(pnl));

        tradesByStrategy.computeIfAbsent(trade.strategy(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByStrategy.put(trade.strategy(), pnlByStrategy.getOrDefault(trade.strategy(), BigDecimal.ZERO).add(pnl));

        tradesByUser.computeIfAbsent(trade.user(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByUser.put(trade.user(), pnlByUser.getOrDefault(trade.user(), BigDecimal.ZERO).add(pnl));

        tradesByTradeTime.computeIfAbsent(trade.tradeTime(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        tradesByTradeTimeSorted.computeIfAbsent(trade.tradeTime(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByTradeTime.put(trade.tradeTime(), pnlByTradeTime.getOrDefault(trade.tradeTime(), BigDecimal.ZERO).add(pnl));

        tradesByValueDate.computeIfAbsent(trade.valueDate(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        tradesByValueDateSorted.computeIfAbsent(trade.valueDate(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByValueDate.put(trade.valueDate(), pnlByValueDate.getOrDefault(trade.valueDate(), BigDecimal.ZERO).add(pnl));
    }

    public ConvertedTrade getTradeById(final String tradeId) {
        return tradesById.get(tradeId);
    }

    public Queue<ConvertedTrade> getTradesByBggCode(final String bggCode) {
        return tradesByBggCode.get(bggCode);
    }

    public BigDecimal getPnlByBggCode(final String bggCode) {
        return pnlByBggCode.get(bggCode);
    }

    public void clear() {
        this.tradesById.clear();
        this.tradesByBggCode.clear();
        this.pnlByBggCode.clear();
        this.tradesByCurrency.clear();
        this.pnlByCurrency.clear();
        this.tradesBySide.clear();
        this.pnlBySide.clear();
        this.tradesByPrice.clear();
        this.tradesByPriceSorted.clear();
        this.pnlByPrice.clear();
        this.tradesByVolume.clear();
        this.tradesByVolumeSorted.clear();
        this.pnlByVolume.clear();
        this.tradesByPortfolio.clear();
        this.pnlByPortfolio.clear();
        this.tradesByAction.clear();
        this.pnlByAction.clear();
        this.tradesByAccount.clear();
        this.pnlByAccount.clear();
        this.tradesByStrategy.clear();
        this.pnlByStrategy.clear();
        this.tradesByUser.clear();
        this.pnlByUser.clear();
        this.tradesByTradeTime.clear();
        this.tradesByTradeTimeSorted.clear();
        this.pnlByTradeTime.clear();
        this.tradesByValueDate.clear();
        this.tradesByValueDateSorted.clear();
        this.pnlByValueDate.clear();
    }
}
