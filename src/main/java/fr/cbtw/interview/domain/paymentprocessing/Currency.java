package fr.cbtw.interview.domain.paymentprocessing;

import java.util.Arrays;
import java.util.Optional;

/** Supported currencies with the hard-coded EUR conversion rates of the legacy (no market data feed). */
public enum Currency {
    EUR(1.0), USD(0.92), GBP(1.17), CHF(1.04);

    private final double eurRate;

    Currency(double eurRate) {
        this.eurRate = eurRate;
    }

    public static Optional<Currency> fromCode(String code) {
        return Arrays.stream(values()).filter(c -> c.name().equals(code)).findFirst();
    }

    public double toEur(double amount) {
        return amount * eurRate;
    }
}
