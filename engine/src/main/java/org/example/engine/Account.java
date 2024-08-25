package org.example.engine;

import org.example.shared.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;

final class Account {

    private static final Logger LOG = LoggerFactory.getLogger(Account.class);

    private final ConcurrentHashMap<String, Portfolio> portfolioMap = new ConcurrentHashMap<>();

    public void registerTrade(final Trade trade, final String portfolioString, final String strategyString,
                              final String bbgCode) {
        final Portfolio portfolio = portfolioMap.computeIfAbsent(portfolioString, key -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("creating new portfolio for trade {}", trade);
            }
            return new Portfolio();
        });
        portfolio.registerTrade(trade, strategyString, bbgCode);
    }

    public double getPnL() {
        final DoubleAdder pnL = new DoubleAdder();
        for (final Map.Entry<String, Portfolio> entry : portfolioMap.entrySet()) {
            final double portfolioPnL = entry.getValue().getPnL();
            if (LOG.isDebugEnabled()) {
                LOG.debug("[portfolio={},PnL={}]", entry.getKey(), portfolioPnL);
            }
            pnL.add(portfolioPnL);
        }
        return pnL.sum();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("org.example.engine.Account=[");
        for (final Map.Entry<String, Portfolio> entry : portfolioMap.entrySet()) {
            sb.append("portfolio=[");
            sb.append(entry.getKey());
            sb.append(",");
            sb.append(entry.getValue());
            sb.append(",total PnL=");
            sb.append(entry.getValue().getPnL());
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }
}
