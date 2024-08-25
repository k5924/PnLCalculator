package org.example.engine;

import org.example.shared.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;

final class User {

    private static final Logger LOG = LoggerFactory.getLogger(User.class);

    private final ConcurrentHashMap<String, Account> accountMap = new ConcurrentHashMap<>();

    public void registerTrade(final Trade trade, final String accountString, final String portfolioString,
                              final String strategyString, final String bbgCode) {
        final Account account = accountMap.computeIfAbsent(accountString, key -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("creating new account for trade {}", trade);
            }
            return new Account();
        });
        account.registerTrade(trade, portfolioString, strategyString, bbgCode);
    }

    public double getPnL() {
        final DoubleAdder pnL = new DoubleAdder();
        for (final Map.Entry<String, Account> entry : accountMap.entrySet()) {
            final double accountPnL = entry.getValue().getPnL();
            if (LOG.isDebugEnabled()) {
                LOG.debug("[account={},PnL={}]", entry.getKey(), accountPnL);
            }
            pnL.add(accountPnL);
        }
        return pnL.sum();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("org.example.engine.User=[");
        for (final Map.Entry<String, Account> entry : accountMap.entrySet()) {
            sb.append("account=[");
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
