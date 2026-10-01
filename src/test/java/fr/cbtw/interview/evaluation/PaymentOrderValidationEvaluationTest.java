package fr.cbtw.interview.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.PaymentOrderValidationUseCase;
import fr.cbtw.interview.infrastructure.InMemoryPaymentOrderRepository;
import fr.cbtw.interview.legacy.PaymentOrderValidationService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Tests de l'évaluateur — NE PAS livrer dans un pad candidat (cf. NOTICE.md).
 * À copier dans le pad après la session pour mesurer ce que les tests fournis ne couvrent pas.
 */
@DisplayName("[Évaluation] Validation d'un ordre de paiement")
class PaymentOrderValidationEvaluationTest {
    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final String DEBTOR = "FR76300010079412";
    private static final String CREDITOR = "DE89370400440532";

    private final PaymentOrderValidationUseCase legacy = new PaymentOrderValidationService();

    private static PaymentOrderValidationUseCase candidate() {
        return ImplementationLoader.findImplementationOf(PaymentOrderValidationUseCase.class);
    }

    // Piège principal : Math.round(19.99 * 100) != 19.99 * 100 en virgule flottante, donc des montants
    // parfaitement valides sont refusés. Sujet d'échange : reproduire (parité) ou corriger (BigDecimal,
    // centimes) — et comment le faire valider par le métier.
    @ParameterizedTest(name = "[{index}] montant={0}")
    @CsvSource({"19.99", "1.15", "0.29", "1500.10", "0.01", "100000.005"})
    void decimalCheckMatchesLegacyIncludingFloatingPointArtefacts(double amount) {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        assertEquals(legacy.validate(DEBTOR, CREDITOR, amount, "EUR", monday, false),
            candidate().validate(DEBTOR, CREDITOR, amount, "EUR", monday, false));
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1} : {2} {3} le {4}, urgent={5}")
    @CsvSource(nullValues = "NULL", value = {
        "FR76300010079412, DE89370400440532, 2500,   EUR, NULL,          false", // date absente -> 'in the past'
        "FR76300010079412, DE89370400440532, 100000, EUR, NEXT_MONDAY,   true",  // borne urgente incluse
        "FR76300010079412, DE89370400440532, 2500,   EUR, NEXT_SUNDAY,   true",  // instantané : pas de report
        "FR76300010079412, DE89370400440532, 2500,   eur, NEXT_MONDAY,   false", // casse
        "'   ',            DE89370400440532, 2500,   EUR, NEXT_MONDAY,   false", // compte blanc
        "FR76300010079412, DE89370400440532, -1,     JPY, YESTERDAY,     true",  // ordre des contrôles
        "FR76300010079412, DE89370400440532, 2500,   USD, YESTERDAY,     true",  // urgence avant date
    })
    void edgeCasesMatchLegacy(String debtor, String creditor, double amount, String currency, String date, boolean urgent) {
        LocalDate executionDate = date == null ? null : resolve(date);
        assertEquals(legacy.validate(debtor, creditor, amount, currency, executionDate, urgent),
            candidate().validate(debtor, creditor, amount, currency, executionDate, urgent));
    }

    @Test
    @DisplayName("Bonus : avec une horloge injectée, le cut-off et le report de week-end sont déterministes")
    void cutOffFollowsInjectedClock() {
        LocalDate thursday = LocalDate.of(2026, 10, 1);
        assertEquals("ACCEPTED: execution=2026-10-01;channel=STANDARD",
            withClockAt(thursday.atTime(16, 0)).validate(DEBTOR, CREDITOR, 2500, "EUR", thursday, false));
        assertEquals("ACCEPTED: execution=2026-10-02;channel=STANDARD",
            withClockAt(thursday.atTime(16, 0, 1)).validate(DEBTOR, CREDITOR, 2500, "EUR", thursday, false));
        assertEquals("ACCEPTED: execution=2026-10-02;channel=STANDARD",
            withClockAt(thursday.atTime(14, 30)).validate(DEBTOR, CREDITOR, 2500, "GBP", thursday, false));
        LocalDate friday = thursday.plusDays(1);
        assertEquals("ACCEPTED: execution=2026-10-05;channel=STANDARD",
            withClockAt(friday.atTime(17, 0)).validate(DEBTOR, CREDITOR, 2500, "EUR", friday, false));
        assertEquals("ACCEPTED: execution=2026-10-02;channel=INSTANT",
            withClockAt(friday.atTime(23, 30)).validate(DEBTOR, CREDITOR, 2500, "EUR", friday, true));
        assertEquals("REJECTED: execution date in the past",
            withClockAt(friday.atTime(9, 0)).validate(DEBTOR, CREDITOR, 2500, "EUR", thursday, false));
    }

    @Test
    @DisplayName("Seuls les ordres acceptés sont enregistrés (si l'adapter fourni est utilisé)")
    void onlyAcceptedOrdersAreRecorded() {
        ImplementationLoader.Wiring wiring = ImplementationLoader.wiring();
        PaymentOrderValidationUseCase useCase = wiring.implementationOf(PaymentOrderValidationUseCase.class);
        Optional<InMemoryPaymentOrderRepository> repository = wiring.instanceOf(InMemoryPaymentOrderRepository.class);
        assumeTrue(repository.isPresent(), "L'implémentation n'utilise pas l'adapter fourni");
        LocalDate monday = resolve("NEXT_MONDAY");
        useCase.validate(DEBTOR, CREDITOR, 2500, "EUR", monday, false);
        useCase.validate(DEBTOR, DEBTOR, 2500, "EUR", monday, false);
        useCase.validate(DEBTOR, CREDITOR, 2500, "JPY", monday, false);
        assertEquals(1, repository.get().saved().size());
    }

    private static PaymentOrderValidationUseCase withClockAt(LocalDateTime now) {
        Clock clock = Clock.fixed(now.atZone(PARIS).toInstant(), PARIS);
        ImplementationLoader.Wiring wiring = ImplementationLoader.wiring().provide(Clock.class, clock);
        PaymentOrderValidationUseCase useCase = wiring.implementationOf(PaymentOrderValidationUseCase.class);
        assumeTrue(wiring.hasInjected(Clock.class), "Bonus non réalisé : l'implémentation ne reçoit pas de Clock");
        return useCase;
    }

    private static LocalDate resolve(String symbolicDate) {
        LocalDate today = LocalDate.now();
        return switch (symbolicDate) {
            case "YESTERDAY" -> today.minusDays(1);
            case "NEXT_MONDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            case "NEXT_SUNDAY" -> today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            default -> throw new IllegalArgumentException(symbolicDate);
        };
    }
}
