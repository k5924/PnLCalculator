package org.example.shared;

import java.math.BigDecimal;

public record ConvertedTrade(String tradeId, String bggCode, Currency currency, Side side, BigDecimal price,
                             int volume, String portfolio, Action action, String account, String strategy,
                             String user, String tradeTime, String valueDate) {

}
