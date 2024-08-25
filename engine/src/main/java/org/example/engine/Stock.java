package org.example.engine;

import org.example.shared.Action;
import org.example.shared.Currency;
import org.example.shared.Side;
import org.example.shared.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;

final class Stock {
    private static final Logger LOG = LoggerFactory.getLogger(Stock.class);
    private final ConcurrentHashMap<String, Trade> tradeMap = new ConcurrentHashMap<>();
    private final DoubleAdder PnL = new DoubleAdder();

    public void registerTrade(final Trade trade) {
        tradeMap.compute(trade.tradeId(), (key, existingTrade) -> {
            if (existingTrade == null) {
                if (!trade.action().equals(Action.CANCEL)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Registering new trade {}", trade);
                    }
                    calculatePnLForNewTrade(trade);
                    return trade;
                }
                return null;
            }

            if (trade.timestamp() < existingTrade.timestamp()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("org.example.shared.Trade is not newer than the existing trade for ID {}", trade.tradeId());
                }
                return existingTrade;
            }

            switch (existingTrade.action()) {
                case Action.NEW -> handleNewTrade(existingTrade, trade);
                case Action.AMEND -> handleAmendedTrade(existingTrade, trade);
            }

            return trade.action() == Action.CANCEL ? null : trade;
        });
    }

    public double getPnL() {
        return PnL.sum();
    }

    private void handleNewTrade(final Trade existingTrade, final Trade newTrade) {
        switch (newTrade.action()) {
            case Action.AMEND -> {
                amendPnLForTrade(existingTrade, newTrade);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Amended trade {}", newTrade);
                }
            }
            case Action.CANCEL -> {
                cancelPnLForTrade(existingTrade);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cancelled trade {}", newTrade);
                }
            }
        }
    }

    private void handleAmendedTrade(final Trade existingTrade, final Trade newTrade) {
        if (newTrade.action() == Action.CANCEL) {
            cancelPnLForTrade(existingTrade);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cancelled trade {}", newTrade);
            }
        }
    }

    private void calculatePnLForNewTrade(final Trade trade) {
        final double conversionRate = Currency.conversionRate(trade.currency());
        final int sideMultiplier = Side.conversion(trade.side());
        PnL.add(trade.price() * trade.volume() * conversionRate * sideMultiplier);
    }

    private void amendPnLForTrade(final Trade oldTrade, final Trade newTrade) {
        cancelPnLForTrade(oldTrade);
        calculatePnLForNewTrade(newTrade);
    }

    private void cancelPnLForTrade(final Trade trade) {
        final double conversionRate = Currency.conversionRate(trade.currency());
        final int sideMultiplier = Side.conversion(trade.side());
        PnL.add(-trade.price() * trade.volume() * conversionRate * sideMultiplier);
    }
}
