package org.example.engine;

import org.example.shared.Action;
import org.example.shared.ConvertedTrade;
import org.example.shared.Currency;
import org.example.shared.Side;

import java.math.BigDecimal;
import java.util.*;

public final class TradeQueryingService {

    private final Map<String, ConvertedTrade> tradesById;
    private final Map<String, List<ConvertedTrade>> tradesByBggCode;
    private final Map<Currency, List<ConvertedTrade>> tradesByCurrency;
    private final Map<Side, List<ConvertedTrade>> tradesBySide;
    private final Map<BigDecimal, List<ConvertedTrade>> tradesByPrice;
    private final Map<Integer, List<ConvertedTrade>> tradesByVolume;
    private final Map<String, List<ConvertedTrade>> tradesByPortfolio;
    private final Map<Action, List<ConvertedTrade>> tradesByAction;
    private final Map<String, List<ConvertedTrade>> tradesByAccount;
    private final Map<String, List<ConvertedTrade>> tradesByStrategy;
    private final Map<String, List<ConvertedTrade>> tradesByUser;
    private final Map<String, List<ConvertedTrade>> tradesByTradeTime;
    private final Map<String, List<ConvertedTrade>> tradesByValueDate;


    public TradeQueryingService() {
        this.tradesById = new HashMap<>(100000, 0.65F);
        this.tradesByBggCode = new HashMap<>(100000, 0.65F);
        this.tradesByCurrency = new HashMap<>(100000, 0.65F);
        this.tradesBySide = new HashMap<>(100000, 0.65F);
        this.tradesByPrice = new HashMap<>(100000, 0.65F);
        this.tradesByVolume = new HashMap<>(100000, 0.65F);
        this.tradesByPortfolio =  new HashMap<>(100000, 0.65F);
        this.tradesByAction =  new HashMap<>(100000, 0.65F);
        this.tradesByAccount =  new HashMap<>(100000, 0.65F);
        this.tradesByStrategy =  new HashMap<>(100000, 0.65F);
        this.tradesByUser =  new HashMap<>(100000, 0.65F);
        this.tradesByTradeTime =  new HashMap<>(100000, 0.65F);
        this.tradesByValueDate =  new HashMap<>(100000, 0.65F);
    }

    public void makeTradeQueryable(final ConvertedTrade trade) {
        tradesById.put(trade.tradeId(), trade);
        tradesByBggCode.computeIfAbsent(trade.bggCode(), key -> new ArrayList<>()).add(trade);
        tradesByCurrency.computeIfAbsent(trade.currency(), key -> new ArrayList<>()).add(trade);
        tradesBySide.computeIfAbsent(trade.side(), key -> new ArrayList<>()).add(trade);
        tradesByPrice.computeIfAbsent(trade.price(), key -> new ArrayList<>()).add(trade);
        tradesByVolume.computeIfAbsent(trade.volume(), key -> new ArrayList<>()).add(trade);
        tradesByPortfolio.computeIfAbsent(trade.portfolio(), key -> new ArrayList<>()).add(trade);
        tradesByAccount.computeIfAbsent(trade.account(), key -> new ArrayList<>()).add(trade);
        tradesByAction.computeIfAbsent(trade.action(), key -> new ArrayList<>()).add(trade);
        tradesByAccount.computeIfAbsent(trade.account(), key -> new ArrayList<>()).add(trade);
        tradesByStrategy.computeIfAbsent(trade.strategy(), key -> new ArrayList<>()).add(trade);
        tradesByUser.computeIfAbsent(trade.user(), key -> new ArrayList<>()).add(trade);
        tradesByTradeTime.computeIfAbsent(trade.tradeTime(), key -> new ArrayList<>()).add(trade);
        tradesByValueDate.computeIfAbsent(trade.valueDate(), key -> new ArrayList<>()).add(trade);
    }
}
