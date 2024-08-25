package org.example.engine;

import org.example.shared.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;

final class Portfolio {

    private static final Logger LOG = LoggerFactory.getLogger(Portfolio.class);

    private final ConcurrentHashMap<String, Strategy> strategyMap = new ConcurrentHashMap<>();

    public void registerTrade(final Trade trade, final String strategyString, final String bbgCode) {
        final Strategy strategy = strategyMap.computeIfAbsent(strategyString, key -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("creating new strategy for trade: {}", trade);
            }
            return new Strategy();
        });
        strategy.registerTrade(trade, bbgCode);
    }

    public double getPnL() {
        final DoubleAdder pnL = new DoubleAdder();
        for (final Map.Entry<String, Strategy> entry : strategyMap.entrySet()) {
            final double strategyPnL = entry.getValue().getPnL();
            if (LOG.isDebugEnabled()) {
                LOG.debug("[strategy={},PnL={}]", entry.getKey(), strategyPnL);
            }
            pnL.add(strategyPnL);
        }
        return pnL.sum();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("org.example.engine.Portfolio=[");
        for (final Map.Entry<String, Strategy> entry : strategyMap.entrySet()) {
            sb.append("strategy=[");
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
