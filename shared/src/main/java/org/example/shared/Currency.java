package org.example.shared;

import java.util.Arrays;

public enum Currency {
    EUR(new byte[]{'E', 'U', 'R'}),
    GBP(new byte[]{'G', 'B', 'P'}),
    JPY(new byte[]{'J', 'P', 'Y'}),
    KRW(new byte[]{'K', 'R', 'W'}),
    NOK(new byte[]{'N', 'O', 'K'}),
    USD(new byte[]{'U', 'S', 'D'});

    private final byte[] encoding;

    Currency(final byte[] encoding) {
        this.encoding = encoding;
    }

    public boolean matches(final byte[] input) {
        return Arrays.equals(input, encoding);
    }
}
