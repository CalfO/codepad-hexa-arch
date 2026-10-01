package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.PaymentOrderProcessingUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Le legacy lit l'heure système : impossible de tester ses cut-offs autrement qu'à l'heure où le test tourne.
 * Ici, le harness injecte une horloge fixe (java.time.Clock) dans votre implémentation ; les résultats
 * attendus sont ceux que le legacy produirait à ce moment-là.
 */
@DisplayName("Traitement des ordres de paiement — cut-offs et calendrier à horloge fixe")
class PaymentOrderProcessingCutOffTest {
    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final String FR = "FR7630001007941234567890185";
    private static final String DE = "DE89370400440532013000";
    private static final String GB = "GB29NWBK60161331926819";
    private static final String BR = "BR1800360305000010009795493C1";

    @ParameterizedTest(name = "[{index}] maintenant={0}, exécution demandée={1} -> {7}")
    @CsvSource(nullValues = "NULL", value = {
        // maintenant (Paris), date demandée, créancier, BIC, montant, devise, urgent, réponse attendue
        "2026-10-01T15:59, 2026-10-01, DE, NULL,     1500,   EUR, false, ACCEPTED;scheme=SEPA;execution=2026-10-01;fees=0.00",
        "2026-10-01T16:00, 2026-10-01, DE, NULL,     1500,   EUR, false, ACCEPTED;scheme=SEPA;execution=2026-10-02;fees=0.00",
        "2026-10-02T16:30, 2026-10-02, DE, NULL,     1500,   EUR, false, ACCEPTED;scheme=SEPA;execution=2026-10-05;fees=0.00",
        "2026-10-01T16:30, 2026-10-01, DE, NULL,     250000, EUR, true,  ACCEPTED;scheme=TARGET2;execution=2026-10-01;fees=15.00",
        "2026-10-01T17:00, 2026-10-01, DE, NULL,     250000, EUR, true,  ACCEPTED;scheme=TARGET2;execution=2026-10-02;fees=15.00",
        "2026-10-01T14:59, 2026-10-01, BR, BRASBRRJ, 1000,   USD, false, ACCEPTED;scheme=SWIFT;execution=2026-10-01;fees=25.92",
        "2026-10-01T15:00, 2026-10-01, BR, BRASBRRJ, 1000,   USD, false, ACCEPTED;scheme=SWIFT;execution=2026-10-02;fees=25.92",
        "2026-10-01T13:00, 2026-10-01, GB, NWBKGB2L, 1000,   GBP, false, ACCEPTED;scheme=SWIFT;execution=2026-10-02;fees=26.17",
        "2026-10-03T23:00, 2026-10-03, DE, NULL,     1500,   EUR, true,  ACCEPTED;scheme=SEPA_INSTANT;execution=2026-10-03;fees=0.50",
        "2026-10-01T10:00, NULL,       DE, NULL,     1500,   EUR, false, ACCEPTED;scheme=SEPA;execution=2026-10-01;fees=0.00",
        "2026-12-24T16:00, 2026-12-24, DE, NULL,     1500,   EUR, false, ACCEPTED;scheme=SEPA;execution=2026-12-28;fees=0.00",
        "2026-12-31T18:00, 2026-12-31, DE, NULL,     1500,   EUR, false, ACCEPTED;scheme=SEPA;execution=2027-01-04;fees=0.00",
        "2026-10-01T09:00, 2026-09-30, DE, NULL,     1500,   EUR, false, REJECTED: execution date in the past",
    })
    void cutOffAndCalendarFollowTheInjectedClock(LocalDateTime now, LocalDate requested, String creditorCountry,
                                                 String bic, double amount, String currency, boolean urgent,
                                                 String expected) {
        ImplementationLoader.Wiring wiring = ImplementationLoader.wiring()
            .provide(Clock.class, Clock.fixed(now.atZone(PARIS).toInstant(), PARIS));
        PaymentOrderProcessingUseCase useCase = wiring.implementationOf(PaymentOrderProcessingUseCase.class);
        assertTrue(wiring.hasInjected(Clock.class),
            "Votre implémentation doit recevoir l'heure courante par injection d'un java.time.Clock (constructeur)");
        String creditor = switch (creditorCountry) {
            case "DE" -> DE;
            case "GB" -> GB;
            case "BR" -> BR;
            default -> throw new IllegalArgumentException(creditorCountry);
        };
        assertEquals(expected, useCase.submit("REF", FR, creditor, bic, amount, currency, requested, urgent));
    }
}
