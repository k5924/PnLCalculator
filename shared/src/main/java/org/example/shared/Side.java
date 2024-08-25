package org.example.shared;

public enum Side {
    B,
    S;

    public static Side from(final String sideString) {
        if (B.toString().equals(sideString)) {
            return B;
        } else if (S.toString().equals(sideString)) {
            return S;
        } else {
            throw new IllegalArgumentException(sideString + " is not supported");
        }
    }

    public static int conversion(final Side side) {
        if (side.equals(B)) {
            return -1;
        } else {
            return 1;
        }
    }
}
