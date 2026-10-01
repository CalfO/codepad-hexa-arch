package fr.cbtw.interview.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.cbtw.interview.application.port.in.PaymentOrderProcessingUseCase;
import fr.cbtw.interview.legacy.PaymentOrderProcessingService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Tests de l'évaluateur — NE PAS livrer dans un pad candidat (cf. NOTICE.md).
 * À copier dans le pad après la session pour mesurer ce que les tests fournis ne couvrent pas.
 */
@DisplayName("[Évaluation] Traitement des ordres de paiement")
class PaymentOrderProcessingEvaluationTest {
    private static final String FR = "FR7630001007941234567890185";
    private static final String DE = "DE89370400440532013000";
    private static final String GB = "GB29NWBK60161331926819";
    private static final LocalDate MONDAY = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    private PaymentOrderProcessingUseCase legacy;
    private PaymentOrderProcessingUseCase candidate;

    @BeforeEach
    void freshInstances() throws ReflectiveOperationException {
        Field savedOrders = PaymentOrderProcessingService.class.getDeclaredField("savedOrders");
        savedOrders.setAccessible(true);
        ((List<?>) savedOrders.get(null)).clear();
        legacy = new PaymentOrderProcessingService();
        candidate = ImplementationLoader.findImplementationOf(PaymentOrderProcessingUseCase.class);
    }

    private void assertSameOutcome(String reference, String debtor, String creditor, String bic, double amount,
                                   String currency, LocalDate date, boolean urgent) {
        assertEquals(legacy.submit(reference, debtor, creditor, bic, amount, currency, date, urgent),
            candidate.submit(reference, debtor, creditor, bic, amount, currency, date, urgent));
    }

    @ParameterizedTest(name = "montant={0}")
    @ValueSource(doubles = {19.99, 1.15, 0.29, 1500.10})
    @DisplayName("Contrôle des décimales en double : des montants valides sont refusés par le legacy")
    void decimalCheckMatchesLegacy(double amount) {
        assertSameOutcome("DEC", FR, DE, null, amount, "EUR", MONDAY, false);
    }

    @Test
    @DisplayName("Un ordre refusé n'est pas enregistré : sa référence peut être resoumise")
    void rejectedOrderDoesNotConsumeItsReference() {
        assertSameOutcome("R-1", FR, GB, null, 1500, "GBP", MONDAY, false);
        assertSameOutcome("R-1", FR, GB, "NWBKGB2L", 1500, "GBP", MONDAY, false);
        assertSameOutcome("R-1", FR, GB, "NWBKGB2L", 1500, "GBP", MONDAY, false);
    }

    @Test
    @DisplayName("Un ordre en attente d'approbation compte dans le plafond et bloque sa référence")
    void pendingOrderCountsTowardLimitAndKeepsItsReference() {
        assertSameOutcome("P-1", FR, DE, null, 1_200_000, "EUR", MONDAY, false);
        assertSameOutcome("P-1", FR, DE, null, 10, "EUR", MONDAY, false);
        assertSameOutcome("P-2", FR, DE, null, 10, "EUR", MONDAY, false);
    }

    @Test
    @DisplayName("Plafond atteint exactement : accepté (comparaison stricte)")
    void limitReachedExactlyIsAccepted() {
        assertSameOutcome("L-1", FR, DE, null, 600_000, "EUR", MONDAY, false);
        assertSameOutcome("L-2", FR, DE, null, 400_000, "EUR", MONDAY, false);
        assertSameOutcome("L-3", FR, DE, null, 0.01, "EUR", MONDAY, false);
    }

    @Test
    @DisplayName("Bornes : instantané à 100 000 inclus, BIC de 9 caractères, EUR vers le Royaume-Uni en SWIFT")
    void boundaries() {
        assertSameOutcome("X-1", FR, DE, null, 100_000, "EUR", MONDAY, true);
        assertSameOutcome("X-2", FR, DE, null, 100_000.01, "EUR", MONDAY, true);
        assertSameOutcome("X-3", FR, GB, "NWBKGB2LX", 1500, "EUR", MONDAY, false);
        assertSameOutcome("X-4", FR, GB, "NWBKGB2L", 1500, "EUR", MONDAY, false);
    }
}
