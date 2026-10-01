package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        return ImplementationLoader.findImplementationOf(LoanSimulationUseCase.class);
    }

    @Test
    @DisplayName("Un emprunteur éligible est approuvé")
    void approvesEligibleApplicant() {
        LoanSimulationUseCase useCase = loadCandidateImplementation();
        String result = useCase.simulate(150000, 3.5, 240, 35, 3000);
        assertEquals("APPROVED: monthly=869.9395769746376", result);
    }

    @Test
    @DisplayName("Un emprunteur mineur est rejeté")
    void rejectsUnderageApplicant() {
        LoanSimulationUseCase useCase = loadCandidateImplementation();
        String result = useCase.simulate(150000, 3.5, 240, 17, 3000);
        assertEquals("ERROR: applicant not eligible", result);
    }
}
