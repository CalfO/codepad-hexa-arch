package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import fr.cbtw.interview.application.port.in.LoanSimulationUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Simulation de prêt")
public class LoanSimulationBehaviorTest {
    private LoanSimulationUseCase loadCandidateImplementation() {
        // Le candidat doit annoter/enregistrer son implémentation, ou on la charge
        // par réflexion en scannant le package domain.service à la recherche
        // d'une classe qui implémente LoanSimulationUseCase.
        return ImplementationLoader.findImplementationOf(LoanSimulationUseCase.class);
    }

    @Test
    @DisplayName("Un emprunteur éligible est approuvé")
    void approvesEligibleApplicant() {
        LoanSimulationUseCase useCase = loadCandidateImplementation();
        String result = useCase.simulate(150000, 3.5, 240, 35, 3000);
        assertTrue(result.contains("APPROVED"));
    }

    @Test
    @DisplayName("Un emprunteur mineur est rejeté")
    void rejectsUnderageApplicant() {
        LoanSimulationUseCase useCase = loadCandidateImplementation();
        String result = useCase.simulate(150000, 3.5, 240, 17, 3000);
        assertTrue(result.contains("ERROR") || result.contains("REJECTED"));
    }

    // Previously untested: the debt-ratio-too-high branch was never exercised by the
    // original two tests.
    @Test
    @DisplayName("Un emprunteur avec un taux d'endettement excessif est rejeté")
    void rejectsApplicantWithExcessiveDebtRatio() {
        LoanSimulationUseCase useCase = loadCandidateImplementation();
        String result = useCase.simulate(400000, 4.5, 240, 35, 1500);
        assertEquals("REJECTED: debt ratio too high", result);
    }

    // Previously untested edge case: a 0% interest rate divides by zero in the amortization
    // formula, producing NaN. The legacy code never guards against this, and NaN comparisons
    // are always false, so the simulation is silently reported as APPROVED. This test locks
    // in that (surprising, arguably buggy) legacy behavior rather than "fixing" it, per the
    // exercise's no-behavior-change constraint. See SOLUTION.md.
    @Test
    @DisplayName("Un taux d'intérêt à 0 % produit une mensualité indéfinie (NaN), conformément au legacy")
    void zeroInterestRateProducesUndefinedMonthlyPayment() {
        LoanSimulationUseCase useCase = loadCandidateImplementation();
        String result = useCase.simulate(150000, 0, 240, 35, 3000);
        assertEquals("APPROVED: monthly=NaN", result);
    }
}
