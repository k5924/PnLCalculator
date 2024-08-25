package org.example.engine;

import org.example.shared.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UserStore {

    private static final Logger LOG = LoggerFactory.getLogger(UserStore.class);

    private final ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<>();

    public void registerTrade(final Trade trade, final String userString, final String accountString,
                              final String portfolioString,
                              final String strategyString,
                              final String bbgCode) {
        if (trade != null) {
            final User user = userMap.computeIfAbsent(userString, key -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("creating new user for trade {}", trade);
                }
                return new User();
            });
            user.registerTrade(trade, accountString, portfolioString, strategyString, bbgCode);
        } else {
            LOG.warn("not registering trade as was null");
        }
    }

    public void getAllPnLData() {
        for (final Map.Entry<String, User> entry : userMap.entrySet()) {
            System.out.println("[user=" + entry.getKey() + ",object=" + entry.getValue() + ",total PnL=" +
                    entry.getValue().getPnL() + "]");
        }
    }
}
