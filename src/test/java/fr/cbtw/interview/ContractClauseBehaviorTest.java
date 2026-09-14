package fr.cbtw.interview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fr.cbtw.interview.application.port.in.ContractClauseUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

class ContractClauseBehaviorTest {

    private ContractClauseUseCase loadCandidateImplementation() {
        return ImplementationLoader.findImplementationOf(ContractClauseUseCase.class);
    }

    @Test
    void standardLowRiskContractOnlyHasStandardTerms() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(100000, 120, "LOW", false, false);
        assertEquals("CLAUSES:STANDARD_TERMS", result);
    }

    @Test
    void highRiskWithoutCoBorrowerRequiresGuarantorAndLifeInsurance() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(150000, 180, "HIGH", false, false);
        assertTrue(result.contains("ADDITIONAL_GUARANTOR_REQUIRED"));
        assertTrue(result.contains("MANDATORY_LIFE_INSURANCE"));
    }

    @Test
    void largeLoanRequiresMortgageGuarantee() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(250000, 120, "LOW", false, false);
        assertTrue(result.contains("MORTGAGE_GUARANTEE_REQUIRED"));
    }

    @Test
    void invalidContractParametersAreRejected() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(-1, 120, "LOW", false, false);
        assertTrue(result.contains("ERROR"));
    }

    // Previously untested: an unrecognized risk profile code must be rejected explicitly,
    // distinct from the invalid-parameters branch above.
    @Test
    void unknownRiskProfileIsRejected() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(100000, 120, "VERY_HIGH", false, false);
        assertEquals("ERROR: unknown risk profile", result);
    }

    // Previously untested: the first-time-buyer discount clause, only granted when the loan
    // also stays within the standard (non-mortgage-guarantee) threshold.
    @Test
    void firstTimeBuyerWithinThresholdGetsReducedFeesClause() {
        ContractClauseUseCase useCase = loadCandidateImplementation();
        String result = useCase.buildContractSummary(150000, 120, "LOW", false, true);
        assertTrue(result.contains("FIRST_TIME_BUYER_REDUCED_FEES"));
    }
}