package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import fr.cbtw.interview.application.port.in.ContractClauseUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Clauses contractuelles")
class ContractClauseBehaviorTest {

    private ContractClauseUseCase loadCandidateImplementation() {
        return ImplementationLoader.findImplementationOf(ContractClauseUseCase.class);
    }

    @Test
    @DisplayName("Un contrat à faible risque standard ne comporte que la clause STANDARD_TERMS")
    void standardLowRiskContractOnlyHasStandardTerms() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(100000, 120, "LOW", false, false);
        assertEquals("CLAUSES:STANDARD_TERMS", result);
    }

    @Test
    @DisplayName("Un contrat à risque élevé sans co-emprunteur exige un garant et une assurance-vie")
    void highRiskWithoutCoBorrowerRequiresGuarantorAndLifeInsurance() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(150000, 180, "HIGH", false, false);
        assertEquals("CLAUSES:STANDARD_TERMS,ADDITIONAL_GUARANTOR_REQUIRED,MANDATORY_LIFE_INSURANCE", result);
    }

    @Test
    @DisplayName("Un prêt d'un montant élevé exige une garantie hypothécaire")
    void largeLoanRequiresMortgageGuarantee() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(250000, 120, "LOW", false, false);
        assertEquals("CLAUSES:STANDARD_TERMS,MORTGAGE_GUARANTEE_REQUIRED", result);
    }

    @Test
    @DisplayName("Des paramètres de contrat invalides sont rejetés")
    void invalidContractParametersAreRejected() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(-1, 120, "LOW", false, false);
        assertEquals("ERROR: invalid contract parameters", result);
    }
}
