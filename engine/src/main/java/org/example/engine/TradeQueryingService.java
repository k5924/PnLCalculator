package org.example.engine;

import org.example.shared.*;
import org.example.shared.Currency;
import org.example.shared.interfaces.Reusable;

import java.math.BigDecimal;
import java.util.concurrent.*;

public final class TradeQueryingService implements Reusable {

    private final ConcurrentMap<String, ConvertedTrade> tradesById;
    private final ConcurrentMap<String, BigDecimal> pnlById;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<ConvertedTrade>> tradesByBggCode;
    private final ConcurrentMap<String, BigDecimal> pnlByBggCode;
    private final ConcurrentMap<Currency, ConcurrentLinkedQueue<ConvertedTrade>> tradesByCurrency;
    private final ConcurrentMap<Currency, BigDecimal> pnlByCurrency;
    private final ConcurrentMap<Side, ConcurrentLinkedQueue<ConvertedTrade>> tradesBySide;
    private final ConcurrentMap<Side, BigDecimal> pnlBySide;
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


    public TradeQueryingService(final int numberOfProcessors) {
        this.tradesById = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.pnlById = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByBggCode = new ConcurrentHashMap<>( 100000, 0.65F, numberOfProcessors);
        this.pnlByBggCode = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.tradesByCurrency = new ConcurrentHashMap<>(Currency.values().length, 0.65F, numberOfProcessors);
        this.pnlByCurrency = new ConcurrentHashMap<>(Currency.values().length, 0.65F, numberOfProcessors);
        this.tradesBySide = new ConcurrentHashMap<>(Side.values().length, 0.65F, numberOfProcessors);
        this.pnlBySide = new ConcurrentHashMap<>(Side.values().length, 0.65F, numberOfProcessors);
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
    }

    public void indexById(final ConvertedTrade trade) {
        final int multiplier = trade.side().multiplier();
        final double fxToUsd = trade.currency().conversion();
        final double adjustedVolume = trade.volume() * multiplier * fxToUsd;
        final BigDecimal pnl = trade.price().multiply(BigDecimal.valueOf(adjustedVolume));
        tradesById.put(trade.tradeId(), trade);
        pnlById.put(trade.tradeId(), pnl);
    }

    public void indexByBggCode(final ConvertedTrade trade) {
        tradesByBggCode.computeIfAbsent(trade.bggCode(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByBggCode.put(trade.bggCode(), pnlByBggCode.getOrDefault(trade.bggCode(), BigDecimal.ZERO)
                .add(pnlById.getOrDefault(trade.tradeId(), BigDecimal.ZERO)));
    }

    public void indexByCurrency(final ConvertedTrade trade) {
        tradesByCurrency.computeIfAbsent(trade.currency(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByCurrency.put(trade.currency(), pnlByCurrency.getOrDefault(trade.currency(), BigDecimal.ZERO)
                .add(pnlById.getOrDefault(trade.tradeId(), BigDecimal.ZERO)));
    }

    public void indexBySide(final ConvertedTrade trade) {
        tradesBySide.computeIfAbsent(trade.side(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlBySide.put(trade.side(), pnlBySide.getOrDefault(trade.side(), BigDecimal.ZERO)
                .add(pnlById.getOrDefault(trade.tradeId(), BigDecimal.ZERO)));
    }

    public void indexByPortfolio(final ConvertedTrade trade) {
        tradesByPortfolio.computeIfAbsent(trade.portfolio(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByPortfolio.put(trade.portfolio(), pnlByPortfolio.getOrDefault(trade.portfolio(), BigDecimal.ZERO)
                .add(pnlById.getOrDefault(trade.tradeId(), BigDecimal.ZERO)));
    }

    public void indexByAction(final ConvertedTrade trade) {
        tradesByAction.computeIfAbsent(trade.action(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByAction.put(trade.action(), pnlByAction.getOrDefault(trade.action(), BigDecimal.ZERO)
                .add(pnlById.getOrDefault(trade.tradeId(), BigDecimal.ZERO)));
    }

    public void indexByAccount(final ConvertedTrade trade) {
        tradesByAccount.computeIfAbsent(trade.account(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByAccount.put(trade.account(), pnlByAccount.getOrDefault(trade.account(), BigDecimal.ZERO)
                .add(pnlById.getOrDefault(trade.tradeId(), BigDecimal.ZERO)));
    }

    public void indexByStrategy(final ConvertedTrade trade) {
        tradesByStrategy.computeIfAbsent(trade.strategy(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByStrategy.put(trade.strategy(), pnlByStrategy.getOrDefault(trade.strategy(), BigDecimal.ZERO)
                .add(pnlById.getOrDefault(trade.tradeId(), BigDecimal.ZERO)));
    }

    public void indexByUser(final ConvertedTrade trade) {
        tradesByUser.computeIfAbsent(trade.user(), key -> new ConcurrentLinkedQueue<>()).add(trade);
        pnlByUser.put(trade.user(), pnlByUser.getOrDefault(trade.user(), BigDecimal.ZERO)
                .add(pnlById.getOrDefault(trade.tradeId(), BigDecimal.ZERO)));
    }

    @Override
    public void clear() {
        this.tradesById.clear();
        this.pnlById.clear();
        this.tradesByBggCode.clear();
        this.pnlByBggCode.clear();
        this.tradesByCurrency.clear();
        this.pnlByCurrency.clear();
        this.tradesBySide.clear();
        this.pnlBySide.clear();
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
    }
}
