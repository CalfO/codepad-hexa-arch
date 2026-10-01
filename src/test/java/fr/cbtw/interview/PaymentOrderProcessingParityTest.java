package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import fr.cbtw.interview.application.port.in.PaymentOrderProcessingUseCase;
import fr.cbtw.interview.legacy.PaymentOrderProcessingService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Double run en miniature, par scénario : le legacy est à état (doublons, plafond journalier), donc chaque
 * scénario rejoue une séquence de soumissions sur un legacy remis à zéro et sur une implémentation neuve,
 * et compare chaque réponse au caractère près.
 */
@DisplayName("Traitement des ordres de paiement — parité avec le legacy")
class PaymentOrderProcessingParityTest {
    private static final String FR = "FR7630001007941234567890185";
    private static final String FR2 = "FR1420041010050500013M02606";
    private static final String DE = "DE89370400440532013000";
    private static final String IT = "IT60X0542811101000000123456";
    private static final String GB = "GB29NWBK60161331926819";
    private static final String CH = "CH9300762011623852957";
    private static final String BR = "BR1800360305000010009795493C1";

    record Submission(String reference, String debtor, String creditor, String bic, double amount, String currency,
                      String date, boolean urgent) {
    }

    static Submission order(String reference, String debtor, String creditor, String bic, double amount,
                            String currency, String date, boolean urgent) {
        return new Submission(reference, debtor, creditor, bic, amount, currency, date, urgent);
    }

    static Stream<Arguments> scenarios() {
        return Stream.of(
            Arguments.of("SEPA simple", List.of(
                order("A-1", FR, DE, null, 1500, "EUR", "NEXT_MONDAY", false),
                order("A-2", DE, IT, null, 250.5, "EUR", "NEXT_TUESDAY", false))),
            Arguments.of("Doublons", List.of(
                order("B-1", FR, DE, null, 1500, "EUR", "NEXT_MONDAY", false),
                order("B-1", FR, DE, null, 1500, "EUR", "NEXT_MONDAY", false),
                order("B-1", GB, DE, null, -1, "JPY", "YESTERDAY", true))),
            Arguments.of("Références absentes", List.of(
                order(null, FR, DE, null, 1500, "EUR", "NEXT_MONDAY", false),
                order("", FR, DE, null, 1500, "EUR", "NEXT_MONDAY", false),
                order("   ", FR, DE, null, 1500, "EUR", "NEXT_MONDAY", false))),
            Arguments.of("IBAN invalides ou identiques", List.of(
                order("D-1", "fr7630001007941234567890185", DE, null, 1500, "EUR", "NEXT_MONDAY", false),
                order("D-2", FR, "DE8937040044", null, 1500, "EUR", "NEXT_MONDAY", false),
                order("D-3", null, DE, null, 1500, "EUR", "NEXT_MONDAY", false),
                order("D-4", FR, null, null, 1500, "EUR", "NEXT_MONDAY", false),
                order("D-5", FR, FR, null, 1500, "EUR", "NEXT_MONDAY", false))),
            Arguments.of("Montants et devises invalides", List.of(
                order("F-1", FR, DE, null, 0, "EUR", "NEXT_MONDAY", false),
                order("F-2", FR, DE, null, -5, "EUR", "NEXT_MONDAY", false),
                order("F-3", FR, DE, null, 10.123, "EUR", "NEXT_MONDAY", false),
                order("F-4", FR, DE, null, 1500, "JPY", "NEXT_MONDAY", false),
                order("F-5", FR, DE, null, 1500, null, "NEXT_MONDAY", false))),
            Arguments.of("Schémas et frais", List.of(
                order("H-1", FR, IT, null, 50000, "EUR", "NEXT_SATURDAY", true),
                order("H-2", FR, DE, null, 250000, "EUR", "NEXT_SATURDAY", true),
                order("H-3", FR, GB, "NWBKGB2L", 1500, "EUR", "NEXT_MONDAY", false),
                order("H-4", FR, GB, null, 1500, "EUR", "NEXT_MONDAY", false),
                order("H-5", FR, BR, "BRASBRRJ", 10000, "USD", "NEXT_MONDAY", true),
                order("H-6", FR, BR, "BRASBRRJXXX", 200000, "USD", "NEXT_MONDAY", false),
                order("H-7", FR, CH, "UBSWCHZH80A", 7200.25, "CHF", "NEXT_SUNDAY", false),
                order("H-8", FR, GB, "NWBKGB2L", 18000.5, "GBP", "NEXT_FRIDAY", true),
                order("H-9", FR, DE, null, 1500, "USD", "NEXT_MONDAY", false))),
            Arguments.of("Plafond journalier par débiteur et date d'exécution", List.of(
                order("M-1", FR, DE, null, 400000, "EUR", "NEXT_TUESDAY", false),
                order("M-2", FR, IT, null, 400000, "EUR", "NEXT_TUESDAY", false),
                order("M-3", FR, DE, null, 300000, "EUR", "NEXT_TUESDAY", false),
                order("M-4", FR, DE, null, 10, "EUR", "NEXT_TUESDAY", false),
                order("M-5", FR, DE, null, 300000, "EUR", "NEXT_WEDNESDAY", false),
                order("M-6", FR2, DE, null, 300000, "EUR", "NEXT_TUESDAY", false))),
            Arguments.of("Plafond journalier multi-devises", List.of(
                order("N-1", FR, BR, "BRASBRRJ", 600000, "USD", "NEXT_MONDAY", false),
                order("N-2", FR, GB, "NWBKGB2L", 400000, "GBP", "NEXT_MONDAY", false),
                order("N-3", FR, DE, null, 1, "EUR", "NEXT_MONDAY", false))),
            Arguments.of("Report d'un samedi : les ordres du lundi s'additionnent", List.of(
                order("S-1", FR, DE, null, 700000, "EUR", "NEXT_SATURDAY", false),
                order("S-2", FR, DE, null, 400000, "EUR", "MONDAY_AFTER_NEXT_SATURDAY", false))),
            Arguments.of("Date d'exécution passée", List.of(
                order("O-1", FR, DE, null, 1500, "EUR", "YESTERDAY", false),
                order("O-2", FR, IT, null, 1500, "EUR", "YESTERDAY", true)))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void producesExactlyTheSameResultsAsLegacy(String scenario, List<Submission> submissions) throws Exception {
        resetLegacyState();
        PaymentOrderProcessingUseCase legacy = new PaymentOrderProcessingService();
        PaymentOrderProcessingUseCase candidate = ImplementationLoader.findImplementationOf(PaymentOrderProcessingUseCase.class);
        for (Submission s : submissions) {
            LocalDate date = resolve(s.date());
            assertEquals(
                legacy.submit(s.reference(), s.debtor(), s.creditor(), s.bic(), s.amount(), s.currency(), date, s.urgent()),
                candidate.submit(s.reference(), s.debtor(), s.creditor(), s.bic(), s.amount(), s.currency(), date, s.urgent()),
                scenario + " / " + s);
        }
    }

    /** Le legacy garde ses ordres dans un champ statique : on le vide entre deux scénarios. */
    private static void resetLegacyState() throws ReflectiveOperationException {
        Field savedOrders = PaymentOrderProcessingService.class.getDeclaredField("savedOrders");
        savedOrders.setAccessible(true);
        ((List<?>) savedOrders.get(null)).clear();
    }

    static LocalDate resolve(String symbolicDate) {
        if (symbolicDate == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        return switch (symbolicDate) {
            case "TODAY" -> today;
            case "YESTERDAY" -> today.minusDays(1);
            case "NEXT_MONDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            case "NEXT_TUESDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
            case "NEXT_WEDNESDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
            case "NEXT_FRIDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
            case "NEXT_SATURDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            case "NEXT_SUNDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            case "MONDAY_AFTER_NEXT_SATURDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).plusDays(2);
            default -> throw new IllegalArgumentException("Date symbolique inconnue : " + symbolicDate);
        };
    }
}
