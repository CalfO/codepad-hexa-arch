package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.ContractClauseUseCase;
import fr.cbtw.interview.legacy.ContractClauseService;
import fr.cbtw.interview.utils.ImplementationLoader;

/** Double run en miniature : même entrée, même sortie que le legacy, au caractère près. */
@DisplayName("Clauses contractuelles — parité avec le legacy")
class ContractClauseParityTest {
    private final ContractClauseUseCase legacy = new ContractClauseService();

    @ParameterizedTest(name = "[{index}] montant={0} durée={1} risque={2} co-emprunteur={3} primo={4}")
    @CsvSource(nullValues = "NULL", value = {
        // montant, durée (mois), profil de risque, co-emprunteur, primo-accédant
        "100000, 120, LOW,       false, false",
        "100000, 120, MEDIUM,    false, false",
        "150000, 180, HIGH,      false, false",
        "250000, 120, LOW,       false, false",
        "250000, 300, HIGH,      false, false",
        "250000, 300, HIGH,      true,  false",
        "150000, 120, LOW,       true,  false",
        "150000, 120, LOW,       false, true",
        "150000, 300, MEDIUM,    true,  true",
        "350000, 360, MEDIUM,    true,  true",
        "90000,  60,  HIGH,      false, true",
        "180000, 250, HIGH,      true,  true",
        "500000, 120, LOW,       false, true",
        "-1,     120, LOW,       false, false",
        "0,      120, LOW,       false, false",
        "100000, 0,   LOW,       false, false",
        "100000, -6,  MEDIUM,    true,  false",
        "100000, 120, VERY_HIGH, false, false",
        "100000, 120, NULL,      false, false",
        "100000, 120, '',        false, false",
        "-5,     120, UNKNOWN,   false, false",
        "75000,  84,  MEDIUM,    false, false",
    })
    void producesExactlyTheSameResultAsLegacy(double amount, int months, String riskProfile,
                                               boolean coBorrower, boolean firstTimeBuyer) {
        ContractClauseUseCase candidate = ImplementationLoader.findImplementationOf(ContractClauseUseCase.class);
        assertEquals(legacy.buildContractSummary(amount, months, riskProfile, coBorrower, firstTimeBuyer),
            candidate.buildContractSummary(amount, months, riskProfile, coBorrower, firstTimeBuyer));
    }
}
