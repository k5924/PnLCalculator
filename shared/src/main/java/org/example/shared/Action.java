package org.example.shared;

import java.util.Arrays;

public enum Action {
    NEW(new byte[]{'N', 'E', 'W'}),
    AMEND(new byte[]{'A', 'M', 'E', 'N', 'D'}),
    CANCEL(new byte[]{'C', 'A', 'N', 'C', 'E', 'L'});

    private final byte[] encoding;

    Action(final byte[] encoding) {
        this.encoding = encoding;
    }

    public boolean matches(final byte[] input) {
        return Arrays.equals(input, encoding);
    }
}
