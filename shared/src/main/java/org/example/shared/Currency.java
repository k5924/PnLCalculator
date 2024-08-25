package org.example.shared;

public enum Currency {
    EUR,
    GBP,
    JPY,
    KRW,
    NOK,
    USD;

    public static Currency from(final String currencyString)
    {
        if (EUR.toString().equals(currencyString)) {
            return EUR;
        } else if (GBP.toString().equals(currencyString)) {
            return GBP;
        } else if (JPY.toString().equals(currencyString)) {
            return JPY;
        } else if (KRW.toString().equals(currencyString)) {
            return KRW;
        } else if (NOK.toString().equals(currencyString)) {
            return NOK;
        } else if (USD.toString().equals(currencyString)) {
            return USD;
        } else {
            throw new IllegalArgumentException(currencyString + " is not supported");
        }
    }

    public static double conversionRate(final Currency currency) {
        return switch (currency) {
            case EUR -> 1.12;
            case GBP -> 1.32;
            case JPY -> 0.0069;
            case KRW -> 0.00075;
            case NOK -> 0.096;
            default -> 1;
        };
    }
}
