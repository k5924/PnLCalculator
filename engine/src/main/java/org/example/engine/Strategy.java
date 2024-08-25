package org.example.engine;

import org.example.shared.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;

final class Strategy {

    private static final Logger LOG = LoggerFactory.getLogger(Strategy.class);
    private final ConcurrentHashMap<String, Stock> stockMap = new ConcurrentHashMap<>();

    public void registerTrade(final Trade trade, final String bbgCode) {
        final Stock stock = stockMap.computeIfAbsent(bbgCode, key -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("new stock seen for strategy, registering trade {}", trade);
            }
            return new Stock();
        });
        stock.registerTrade(trade);
    }

    public double getPnL() {
        final DoubleAdder pnL = new DoubleAdder();
        for (final Map.Entry<String, Stock> entry : stockMap.entrySet()) {
            final double strategyPnL = entry.getValue().getPnL();
            if (LOG.isDebugEnabled()) {
                LOG.debug("[stock={},PnL={}]", entry.getKey(), strategyPnL);
            }
            pnL.add(strategyPnL);
        }
        return pnL.sum();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("org.example.engine.Strategy=[");
        for (final Map.Entry<String, Stock> entry : stockMap.entrySet()) {
            sb.append("stock=[");
            sb.append(entry.getKey());
            sb.append(",total PnL=");
            sb.append(entry.getValue().getPnL());
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }
}
