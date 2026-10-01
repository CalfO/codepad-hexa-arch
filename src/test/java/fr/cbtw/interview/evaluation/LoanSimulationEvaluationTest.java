package fr.cbtw.interview.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.LoanSimulationUseCase;
import fr.cbtw.interview.infrastructure.InMemoryLoanSimulationRepository;
import fr.cbtw.interview.legacy.LoanSimulationService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Tests de l'évaluateur — NE PAS livrer dans un pad candidat (cf. NOTICE.md).
 * À copier dans le pad après la session pour mesurer ce que les tests fournis ne couvrent pas.
 */
@DisplayName("[Évaluation] Simulation de prêt")
class LoanSimulationEvaluationTest {
    private final LoanSimulationUseCase legacy = new LoanSimulationService();

    private static LoanSimulationUseCase candidate() {
        return ImplementationLoader.findImplementationOf(LoanSimulationUseCase.class);
    }

    @Test
    @DisplayName("Un taux d'endettement excessif est rejeté")
    void rejectsApplicantWithExcessiveDebtRatio() {
        assertEquals("REJECTED: debt ratio too high", candidate().simulate(400000, 4.5, 240, 35, 1500));
    }

    // Piège principal : 0 / (1 - 1) donne NaN, toute comparaison avec NaN est fausse, donc le contrôle
    // d'endettement ne se déclenche jamais. Le legacy approuve avec une mensualité NaN. Sujet d'échange :
    // reproduire (parité stricte) ou corriger (évolution assumée, à faire valider par le métier) ?
    @Test
    @DisplayName("Taux à 0 % : le legacy approuve avec une mensualité NaN")
    void zeroInterestRateProducesUndefinedMonthlyPayment() {
        assertEquals("APPROVED: monthly=NaN", candidate().simulate(150000, 0, 240, 35, 3000));
    }

    @ParameterizedTest(name = "[{index}] montant={0} taux={1} durée={2} âge={3} revenu={4}")
    @CsvSource({
        "150000, 0,    240, 35, 3000",   // NaN
        "150000, 0,    240, 35, 0",      // NaN / 0
        "150000, 3.5,  240, 35, 0",      // ratio infini -> rejeté
        "150000, 3.5,  240, 35, -2000",  // revenu négatif -> ratio négatif -> approuvé
        "150000, 3.5,  240, 18, 3000",   // borne d'âge basse incluse
        "150000, 3.5,  240, 75, 3000",   // borne d'âge haute incluse
        "1,      0.01, 1,   35, 1",      // très petits montants
        "1e9,    3.5,  240, 35, 3000",   // très gros montant
    })
    void edgeCasesMatchLegacy(double amount, double rate, int months, int age, double income) {
        assertEquals(legacy.simulate(amount, rate, months, age, income), candidate().simulate(amount, rate, months, age, income));
    }

    @Test
    @DisplayName("Seules les simulations approuvées sont enregistrées (si l'adapter fourni est utilisé)")
    void onlyApprovedSimulationsAreRecorded() {
        ImplementationLoader.Wiring wiring = ImplementationLoader.wiring();
        LoanSimulationUseCase useCase = wiring.implementationOf(LoanSimulationUseCase.class);
        Optional<InMemoryLoanSimulationRepository> repository = wiring.instanceOf(InMemoryLoanSimulationRepository.class);
        assumeTrue(repository.isPresent(), "L'implémentation n'utilise pas l'adapter fourni");
        useCase.simulate(150000, 3.5, 240, 35, 3000);
        useCase.simulate(400000, 4.5, 240, 35, 1500);
        useCase.simulate(150000, 3.5, 240, 17, 3000);
        assertEquals(1, repository.get().saved().size());
    }
}
