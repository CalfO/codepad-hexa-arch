package fr.cbtw.interview.domain.paymentprocessing;

import java.util.Set;

/**
 * Countries the legacy treats as SEPA-reachable. Deliberately the legacy list, not the real SEPA zone
 * (GB, CH, NO... are missing, so EUR payments there go through SWIFT) — a business question, not a refactor.
 */
public final class SepaZone {
    private static final Set<String> COUNTRIES = Set.of("FR", "DE", "ES", "IT", "BE", "NL", "LU", "PT", "IE", "AT");

    private SepaZone() {
    }

    public static boolean reaches(Iban debtor, Iban creditor, Currency currency) {
        return currency == Currency.EUR
            && COUNTRIES.contains(debtor.countryCode()) && COUNTRIES.contains(creditor.countryCode());
    }
}
