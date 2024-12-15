package org.example.reader.index;

import org.example.engine.TradeQueryingService;
import org.example.reader.query.TradeQueryInserter;
import org.example.shared.*;
import org.example.shared.interfaces.Reusable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public final class TradeIndexingService implements Reusable {

    private final ConcurrentMap<String, ConvertedTrade> newTrades;
    private final ConcurrentMap<String, ConvertedTrade> amendedTrades;
    private final ConcurrentMap<String, ConvertedTrade> cancelledTrades;
    private final DefaultWorkerPool<TradeCleaner> cleanUpCancelledTradesFromNewTrades;
    private final DefaultWorkerPool<TradeCleaner> cleanUpCancelledTradesFromAmendedTrades;
    private final DefaultWorkerPool<TradeCleaner> cleanUpAmendedTradesFromNewTrades;
    private final DefaultWorkerPool<TradeQueryInserter> idInserterPool;
    private final DefaultWorkerPool<TradeQueryInserter> bggInserterPool;
    private final DefaultWorkerPool<TradeQueryInserter> currencyInserterPool;
    private final DefaultWorkerPool<TradeQueryInserter> sideInserterPool;
    private final DefaultWorkerPool<TradeQueryInserter> portfolioInserterPool;
    private final DefaultWorkerPool<TradeQueryInserter> actionInserterPool;
    private final DefaultWorkerPool<TradeQueryInserter> accountInserterPool;
    private final DefaultWorkerPool<TradeQueryInserter> strategyInserterPool;
    private final DefaultWorkerPool<TradeQueryInserter> userInserterPool;


    public TradeIndexingService(final TradeQueryingService tradeQueryingService,
                                final int numberOfProcessors) {
        this.newTrades = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.amendedTrades = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.cancelledTrades = new ConcurrentHashMap<>(100000, 0.65F, numberOfProcessors);
        this.cleanUpCancelledTradesFromNewTrades = new DefaultWorkerPool<>(() -> new TradeCleaner(newTrades), numberOfProcessors);
        this.cleanUpCancelledTradesFromAmendedTrades = new DefaultWorkerPool<>(() -> new TradeCleaner(amendedTrades), numberOfProcessors);
        this.cleanUpAmendedTradesFromNewTrades = new DefaultWorkerPool<>(() -> new TradeCleaner(newTrades), numberOfProcessors);
        this.idInserterPool = new DefaultWorkerPool<>(() -> new TradeQueryInserter(tradeQueryingService::indexById), numberOfProcessors);
        this.bggInserterPool = new DefaultWorkerPool<>(() -> new TradeQueryInserter(tradeQueryingService::indexByBggCode), numberOfProcessors);
        this.currencyInserterPool = new DefaultWorkerPool<>(() -> new TradeQueryInserter(tradeQueryingService::indexByCurrency), numberOfProcessors);
        this.sideInserterPool = new DefaultWorkerPool<>(() -> new TradeQueryInserter(tradeQueryingService::indexBySide), numberOfProcessors);
        this.portfolioInserterPool = new DefaultWorkerPool<>(() -> new TradeQueryInserter(tradeQueryingService::indexByPortfolio), numberOfProcessors);
        this.actionInserterPool = new DefaultWorkerPool<>(() -> new TradeQueryInserter(tradeQueryingService::indexByAction), numberOfProcessors);
        this.accountInserterPool = new DefaultWorkerPool<>(() -> new TradeQueryInserter(tradeQueryingService::indexByAccount), numberOfProcessors);
        this.strategyInserterPool = new DefaultWorkerPool<>(() -> new TradeQueryInserter(tradeQueryingService::indexByStrategy), numberOfProcessors);
        this.userInserterPool = new DefaultWorkerPool<>(() -> new TradeQueryInserter(tradeQueryingService::indexByUser), numberOfProcessors);
    }

    public void indexTrade(final ConvertedTrade trade) {
        switch (trade.action()) {
            case NEW -> newTrades.put(trade.tradeId(), trade);
            case AMEND -> amendedTrades.put(trade.tradeId(), trade);
            case CANCEL -> cancelledTrades.put(trade.tradeId(), trade);
        }
    }

    public void finishProcessingTrades() {
        final Set<String> cancelledTradeIds = cancelledTrades.keySet();
        cleanUpCancelledTradesFromNewTrades.setNumberOfWorkers(cancelledTradeIds.size());
        setupTradeCleaners(cancelledTradeIds, cleanUpCancelledTradesFromNewTrades);
        cleanUpCancelledTradesFromAmendedTrades.setNumberOfWorkers(cancelledTradeIds.size());
        setupTradeCleaners(cancelledTradeIds, cleanUpCancelledTradesFromAmendedTrades);
        cleanUpCancelledTradesFromNewTrades.doWork();
        cleanUpCancelledTradesFromAmendedTrades.doWork();
        final Set<String> amendedTradeIds = amendedTrades.keySet();
        cleanUpAmendedTradesFromNewTrades.setNumberOfWorkers(amendedTradeIds.size());
        setupTradeCleaners(amendedTradeIds, cleanUpAmendedTradesFromNewTrades);
        cleanUpAmendedTradesFromNewTrades.doWork();
    }

    private void setupTradeCleaners(final Set<String> idsToRemove, final DefaultWorkerPool<TradeCleaner> pool) {
        for (final String id : idsToRemove) {
            final TradeCleaner cleaner = pool.get(id.hashCode());
            cleaner.addId(id);
        }
    }

    public void makeTradesQueryable() {
        setupIdInserters();
        idInserterPool.doWork();
        setupBggInserters();
        bggInserterPool.doWork();
        setupCurrencyInserters();
        currencyInserterPool.doWork();
        setupSideInserters();
        sideInserterPool.doWork();
        setupPortfolioInserters();
        portfolioInserterPool.doWork();
        setupActionInserters();
        actionInserterPool.doWork();
        setupAccountInserters();
        accountInserterPool.doWork();
        setupStrategyInserters();
        strategyInserterPool.doWork();
        setupUserInserters();
        userInserterPool.doWork();
    }

    private void setupIdInserters() {
        final int idInserterSize = amendedTrades.size() + newTrades.size();
        insertIntoPool(idInserterSize, (trade) -> trade.tradeId().hashCode(), idInserterPool);
    }

    private void setupBggInserters() {
        final int count = (int) Stream.concat(amendedTrades.values().stream(), newTrades.values().stream())
                .map(ConvertedTrade::bggCode)
                .distinct()
                .count();
        insertIntoPool(count, (trade) -> trade.bggCode().hashCode(), bggInserterPool);
    }

    private void setupCurrencyInserters() {
        insertIntoPool(Currency.values().length, (trade) -> trade.currency().hashCode(), currencyInserterPool);
    }

    private void setupSideInserters() {
        insertIntoPool(Side.values().length, (trade) -> trade.side().hashCode(), sideInserterPool);
    }

    private void setupPortfolioInserters() {
        final int count = (int) Stream.concat(amendedTrades.values().stream(), newTrades.values().stream())
                .map(ConvertedTrade::portfolio)
                .distinct()
                .count();
        insertIntoPool(count, (trade) -> trade.portfolio().hashCode(), portfolioInserterPool);
    }

    private void setupActionInserters() {
        insertIntoPool(Action.values().length, (trade) -> trade.action().hashCode(), actionInserterPool);
    }

    private void setupAccountInserters() {
        final int count = (int) Stream.concat(amendedTrades.values().stream(), newTrades.values().stream())
                .map(ConvertedTrade::account)
                .distinct()
                .count();
        insertIntoPool(count, (trade) -> trade.account().hashCode(), accountInserterPool);
    }

    private void setupStrategyInserters() {
        final int count = (int) Stream.concat(amendedTrades.values().stream(), newTrades.values().stream())
                .map(ConvertedTrade::strategy)
                .distinct()
                .count();
        insertIntoPool(count, (trade) -> trade.strategy().hashCode(), strategyInserterPool);
    }

    private void setupUserInserters() {
        final int count = (int) Stream.concat(amendedTrades.values().stream(), newTrades.values().stream())
                .map(ConvertedTrade::user)
                .distinct()
                .count();
        insertIntoPool(count, (trade) -> trade.user().hashCode(), userInserterPool);
    }

    private void insertIntoPool(final int size, final ToIntFunction<ConvertedTrade> hashCode, final DefaultWorkerPool<TradeQueryInserter> pool) {
        pool.setNumberOfWorkers(size);
        for (final ConvertedTrade trade : amendedTrades.values()) {
            final TradeQueryInserter inserter = pool.get(hashCode.applyAsInt(trade));
            inserter.addTrade(trade);
        }
        for (final ConvertedTrade trade : newTrades.values()) {
            final TradeQueryInserter inserter = pool.get(hashCode.applyAsInt(trade));
            inserter.addTrade(trade);
        }
    }

    @Override
    public void clear() {
        this.newTrades.clear();
        this.amendedTrades.clear();
        this.cancelledTrades.clear();
    }
}
