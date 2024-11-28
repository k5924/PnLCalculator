package org.example.shared;

import org.example.shared.Action;
import org.example.shared.Currency;
import org.example.shared.Side;

import java.math.BigDecimal;

public record ConvertedTrade(String tradeId, String bggCode, Currency currency, Side side, BigDecimal price,
                             int volume, String portfolio, Action action, String account, String strategy,
                             String user, String tradeTime, String valueDate) {

}
