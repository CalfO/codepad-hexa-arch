package fr.cbtw.interview.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.CashSweepingUseCase;
import fr.cbtw.interview.infrastructure.InMemorySweepInstructionRepository;
import fr.cbtw.interview.legacy.CashSweepingService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Tests de l'évaluateur — NE PAS livrer dans un pad candidat (cf. NOTICE.md).
 * À copier dans le pad après la session pour mesurer ce que les tests fournis ne couvrent pas.
 */
@DisplayName("[Évaluation] Règle de sweeping")
class CashSweepingEvaluationTest {
    private final CashSweepingUseCase legacy = new CashSweepingService();

    private static CashSweepingUseCase candidate() {
        return ImplementationLoader.findImplementationOf(CashSweepingUseCase.class);
    }

    // Piège principal : sous-compte déjà à l'équilibre et minimum à 0 -> |0.0| < 0 est faux, on part dans
    // la branche "financement" et -0.0 s'affiche. Le legacy émet une instruction de sweep de "-0.0" (et
    // l'enregistre). Reproduire ou corriger ?
    @Test
    @DisplayName("Sous-compte à l'équilibre, minimum à 0 : le legacy émet FROM_MASTER=-0.0")
    void balancedAccountWithZeroMinimumEmitsNegativeZeroFunding() {
        assertEquals("SWEEP: FROM_MASTER=-0.0", candidate().computeSweep("TARGET_BALANCING", 5000, 5000, 0, 100000));
    }

    @ParameterizedTest(name = "[{index}] {0} solde={1} cible={2} minimum={3} centralisateur={4}")
    @CsvSource({
        "ZERO_BALANCING,   0,        0,     0,    100000",  // -0.0
        "ZERO_BALANCING,   0,        0,     0,    0",       // -0.0 mais refus faute de financement
        "TARGET_BALANCING, 2000,     5000,  0,    1500.123", // partiel : montant non arrondi
        "TARGET_BALANCING, 4900,     5000,  100,  100000",  // |delta| == minimum -> mouvement déclenché
        "THRESHOLD,        20000,    20000, 0,    100000",  // égalité au seuil -> pas de sweep
        "THRESHOLD,        20000.004,20000, 0,    100000",  // au-dessus du seuil mais arrondi à 0.0
        "ZERO_BALANCING,   0.005,    0,     0,    100000",  // arrondi au centime
        "TARGET_BALANCING, 2000,     5000,  100,  3000",    // financement exact (pas partiel)
    })
    void edgeCasesMatchLegacy(String type, double balance, double target, double minimum, double master) {
        assertEquals(legacy.computeSweep(type, balance, target, minimum, master),
            candidate().computeSweep(type, balance, target, minimum, master));
    }

    @Test
    @DisplayName("Seuls les mouvements émis sont enregistrés (si l'adapter fourni est utilisé)")
    void onlyEmittedTransfersAreRecorded() {
        ImplementationLoader.Wiring wiring = ImplementationLoader.wiring();
        CashSweepingUseCase useCase = wiring.implementationOf(CashSweepingUseCase.class);
        Optional<InMemorySweepInstructionRepository> repository = wiring.instanceOf(InMemorySweepInstructionRepository.class);
        assumeTrue(repository.isPresent(), "L'implémentation n'utilise pas l'adapter fourni");
        useCase.computeSweep("ZERO_BALANCING", 12500, 0, 100, 500000);
        useCase.computeSweep("TARGET_BALANCING", 5050, 5000, 100, 500000);
        useCase.computeSweep("TARGET_BALANCING", 2000, 5000, 100, 0);
        useCase.computeSweep("UNKNOWN", 2000, 5000, 100, 0);
        assertEquals(1, repository.get().saved().size());
    }
}
