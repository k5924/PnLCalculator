package org.example.shared;

public enum Side {
    B('B'),
    S('S');

    private final byte encoding;

    Side(final char encoding) {
        this.encoding = (byte) encoding;
    }

    public boolean matches(final byte input) {
        return this.encoding == input;
    }

    public static Side fromByte(final byte input) {
        for (Side side : values()) {
            if (side.matches(input)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown Side for byte: " + (char) input);
    }
}