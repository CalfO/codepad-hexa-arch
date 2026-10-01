package fr.cbtw.interview.domain.paymentprocessing;

import java.time.LocalTime;
import java.util.Optional;

public enum PaymentScheme {
    SEPA_INSTANT, TARGET2, SEPA, SWIFT;

    private static final double INSTANT_LIMIT = 100_000;

    public static PaymentScheme select(boolean sepaReachable, boolean urgent, Money money) {
        if (sepaReachable && urgent && money.amount() <= INSTANT_LIMIT) {
            return SEPA_INSTANT;
        }
        if (sepaReachable && urgent) {
            return TARGET2;
        }
        return sepaReachable ? SEPA : SWIFT;
    }

    /** Empty for instant payments, which run 24/7 and are never rolled. Cut-off is inclusive. */
    public Optional<LocalTime> cutOff(Currency currency) {
        return switch (this) {
            case SEPA_INSTANT -> Optional.empty();
            case SEPA -> Optional.of(LocalTime.of(16, 0));
            case TARGET2 -> Optional.of(LocalTime.of(17, 0));
            case SWIFT -> Optional.of(currency == Currency.USD ? LocalTime.of(15, 0) : LocalTime.of(13, 0));
        };
    }

    public double fees(Money money, boolean urgent) {
        return switch (this) {
            case SEPA -> 0;
            case SEPA_INSTANT -> 0.5;
            case TARGET2 -> 15;
            case SWIFT -> Math.min(25 + money.inEur() * 0.001, 100) + (urgent ? 30 : 0);
        };
    }
}
