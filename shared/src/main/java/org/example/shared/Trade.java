package org.example.shared;

public record Trade(String tradeId,
                    Currency currency,
                    Side side,
                    double price,
                    int volume,
                    Action action,
                    long timestamp) {
}
