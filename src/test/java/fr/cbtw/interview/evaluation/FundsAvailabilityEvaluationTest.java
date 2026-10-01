package fr.cbtw.interview.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.FundsAvailabilityUseCase;
import fr.cbtw.interview.infrastructure.InMemoryFundsReservationRepository;
import fr.cbtw.interview.legacy.FundsAvailabilityService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Tests de l'évaluateur — NE PAS livrer dans un pad candidat (cf. NOTICE.md).
 * À copier dans le pad après la session pour mesurer ce que les tests fournis ne couvrent pas.
 */
@DisplayName("[Évaluation] Contrôle de provision")
class FundsAvailabilityEvaluationTest {
    private final FundsAvailabilityUseCase legacy = new FundsAvailabilityService();

    private static FundsAvailabilityUseCase candidate() {
        return ImplementationLoader.findImplementationOf(FundsAvailabilityUseCase.class);
    }

    // Piège principal : les débits en attente ne sont pas validés. Un montant négatif (des crédits en
    // attente) augmente le disponible et fait autoriser un paiement que le solde ne couvre pas.
    @Test
    @DisplayName("Des débits en attente négatifs augmentent le disponible, comme dans le legacy")
    void negativePendingDebitsIncreaseAvailableFunds() {
        assertEquals("AUTHORIZED: remaining=0.0", candidate().checkFunds("ACTIVE", 1000, -500, 0, 1500));
    }

    @ParameterizedTest(name = "[{index}] {0} solde={1} en-cours={2} découvert={3} paiement={4}")
    @CsvSource({
        "ACTIVE, 1000,  0,    5000, 6000",  // découvert consommé à 100 % -> approbation
        "ACTIVE, 1000,  0,    5000, 5000",  // usage exactement 80 % -> pas d'approbation
        "ACTIVE, 1000,  0,    0,    1000",  // paiement == disponible
        "ACTIVE, 0.3,   0.1,  0,    0.2",   // artefact flottant dans 'remaining'
        "ACTIVE, 1000,  -500, 0,    1500",
        "ACTIVE, -100,  0,    0,    1",     // solde déjà négatif sans découvert
        "BLOCKED,-100,  0,    -5,   0",     // le statut prime sur tout le reste
    })
    void edgeCasesMatchLegacy(String status, double ledger, double pending, double overdraft, double amount) {
        assertEquals(legacy.checkFunds(status, ledger, pending, overdraft, amount),
            candidate().checkFunds(status, ledger, pending, overdraft, amount));
    }

    @Test
    @DisplayName("Seuls les paiements autorisés ou soumis à approbation réservent des fonds (si l'adapter fourni est utilisé)")
    void onlyAcceptedPaymentsReserveFunds() {
        ImplementationLoader.Wiring wiring = ImplementationLoader.wiring();
        FundsAvailabilityUseCase useCase = wiring.implementationOf(FundsAvailabilityUseCase.class);
        Optional<InMemoryFundsReservationRepository> repository = wiring.instanceOf(InMemoryFundsReservationRepository.class);
        assumeTrue(repository.isPresent(), "L'implémentation n'utilise pas l'adapter fourni");
        useCase.checkFunds("ACTIVE", 10000, 0, 0, 100);
        useCase.checkFunds("ACTIVE", 1000, 0, 5000, 6000);
        useCase.checkFunds("ACTIVE", 1000, 0, 0, 6000);
        useCase.checkFunds("CLOSED", 1000, 0, 0, 10);
        assertEquals(2, repository.get().saved().size());
    }
}
