package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.PaymentOrderValidationUseCase;
import fr.cbtw.interview.legacy.PaymentOrderValidationService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Double run en miniature : même entrée, même sortie que le legacy, au caractère près.
 * Les dates sont symboliques (relatives à aujourd'hui) pour que le test reste valable dans le temps.
 */
@DisplayName("Validation d'un ordre de paiement — parité avec le legacy")
class PaymentOrderValidationParityTest {
    private final PaymentOrderValidationUseCase legacy = new PaymentOrderValidationService();

    @ParameterizedTest(name = "[{index}] {0} -> {1} : {2} {3} le {4}, urgent={5}")
    @CsvSource(nullValues = "NULL", value = {
        // débiteur, créancier, montant, devise, date d'exécution, urgent
        "FR76300010079412, DE89370400440532, 2500,     EUR,  NEXT_MONDAY,   false",
        "FR76300010079412, DE89370400440532, 2500,     EUR,  NEXT_TUESDAY,  false",
        "FR76300010079412, DE89370400440532, 2500,     EUR,  NEXT_SATURDAY, false",
        "FR76300010079412, DE89370400440532, 2500,     EUR,  NEXT_SUNDAY,   false",
        "FR76300010079412, DE89370400440532, 2500,     EUR,  IN_30_DAYS,    false",
        "FR76300010079412, GB29NWBK60161331, 18000.5,  GBP,  NEXT_FRIDAY,   false",
        "FR76300010079412, CH93007620116238, 7200.25,  CHF,  NEXT_SUNDAY,   false",
        "FR76300010079412, US64SVBKUS6S3300, 1000000,  USD,  NEXT_MONDAY,   false",
        "FR76300010079412, DE89370400440532, 99999,    EUR,  NEXT_SATURDAY, true",
        "FR76300010079412, DE89370400440532, 2500,     EUR,  NEXT_MONDAY,   true",
        "FR76300010079412, DE89370400440532, 150000,   EUR,  NEXT_MONDAY,   true",
        "FR76300010079412, GB29NWBK60161331, 2500,     GBP,  NEXT_MONDAY,   true",
        "FR76300010079412, GB29NWBK60161331, 250000,   USD,  NEXT_MONDAY,   true",
        "FR76300010079412, FR76300010079412, 2500,     EUR,  NEXT_MONDAY,   false",
        "NULL,             DE89370400440532, 2500,     EUR,  NEXT_MONDAY,   false",
        "FR76300010079412, '',               2500,     EUR,  NEXT_MONDAY,   false",
        "FR76300010079412, DE89370400440532, 0,        EUR,  NEXT_MONDAY,   false",
        "FR76300010079412, DE89370400440532, -100,     EUR,  NEXT_MONDAY,   false",
        "FR76300010079412, DE89370400440532, 10.123,   EUR,  NEXT_MONDAY,   false",
        "FR76300010079412, DE89370400440532, 2500,     JPY,  NEXT_MONDAY,   false",
        "FR76300010079412, DE89370400440532, 2500,     NULL, NEXT_MONDAY,   false",
        "FR76300010079412, DE89370400440532, 2500,     EUR,  YESTERDAY,     false",
        "FR76300010079412, DE89370400440532, 2500,     EUR,  LAST_YEAR,     true",
        "FR76300010079412, DE89370400440532, 2500,     EUR,  TODAY,         true",
        "FR76300010079412, DE89370400440532, 2500,     EUR,  TODAY,         false",
    })
    void producesExactlyTheSameResultAsLegacy(String debtor, String creditor, double amount, String currency,
                                               String executionDate, boolean urgent) {
        LocalDate date = resolve(executionDate);
        PaymentOrderValidationUseCase candidate = ImplementationLoader.findImplementationOf(PaymentOrderValidationUseCase.class);
        assertEquals(legacy.validate(debtor, creditor, amount, currency, date, urgent),
            candidate.validate(debtor, creditor, amount, currency, date, urgent));
    }

    static LocalDate resolve(String symbolicDate) {
        LocalDate today = LocalDate.now();
        return switch (symbolicDate) {
            case "TODAY" -> today;
            case "YESTERDAY" -> today.minusDays(1);
            case "LAST_YEAR" -> today.minusYears(1);
            case "IN_30_DAYS" -> today.plusDays(30);
            case "NEXT_MONDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            case "NEXT_TUESDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
            case "NEXT_FRIDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
            case "NEXT_SATURDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            case "NEXT_SUNDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            default -> throw new IllegalArgumentException("Date symbolique inconnue : " + symbolicDate);
        };
    }
}
