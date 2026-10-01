package fr.cbtw.interview.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.ContractClauseUseCase;
import fr.cbtw.interview.infrastructure.InMemoryContractRepository;
import fr.cbtw.interview.legacy.ContractClauseService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Tests de l'évaluateur — NE PAS livrer dans un pad candidat (cf. NOTICE.md).
 * À copier dans le pad après la session pour mesurer ce que les tests fournis ne couvrent pas.
 */
@DisplayName("[Évaluation] Clauses contractuelles")
class ContractClauseEvaluationTest {
    private final ContractClauseUseCase legacy = new ContractClauseService();

    private static ContractClauseUseCase candidate() {
        return ImplementationLoader.findImplementationOf(ContractClauseUseCase.class);
    }

    @Test
    @DisplayName("Un profil de risque inconnu est rejeté")
    void unknownRiskProfileIsRejected() {
        assertEquals("ERROR: unknown risk profile", candidate().buildContractSummary(100000, 120, "VERY_HIGH", false, false));
    }

    @Test
    @DisplayName("Un primo-accédant sous le seuil obtient la clause de frais réduits")
    void firstTimeBuyerWithinThresholdGetsReducedFeesClause() {
        assertEquals("CLAUSES:STANDARD_TERMS,FIRST_TIME_BUYER_REDUCED_FEES",
            candidate().buildContractSummary(150000, 120, "LOW", false, true));
    }

    @ParameterizedTest(name = "[{index}] montant={0} durée={1} risque={2} co-emprunteur={3} primo={4}")
    @CsvSource(nullValues = "NULL", value = {
        "200000,    120, LOW,     false, true",  // seuil 200000 : pas de garantie, frais réduits accordés
        "200000.01, 120, LOW,     false, true",  // juste au-dessus
        "100000,    240, LOW,     false, false", // seuil 240 mois exclu
        "100000,    241, LOW,     false, false",
        "100000,    120, low,     false, false", // casse
        "100000,    120, ' HIGH', false, false", // espaces
        "100000,    120, HIGH,    true,  false", // garant + solidarité, pas d'assurance-vie
        "-1,        0,   NULL,    false, false", // ordre des contrôles : paramètres avant profil
        "250000,    300, HIGH,    true,  true",  // toutes les clauses sauf assurance-vie et frais réduits
    })
    void edgeCasesMatchLegacy(double amount, int months, String risk, boolean coBorrower, boolean firstTimeBuyer) {
        assertEquals(legacy.buildContractSummary(amount, months, risk, coBorrower, firstTimeBuyer),
            candidate().buildContractSummary(amount, months, risk, coBorrower, firstTimeBuyer));
    }

    @Test
    @DisplayName("Seuls les contrats valides sont enregistrés (si l'adapter fourni est utilisé)")
    void onlyValidContractsAreRecorded() {
        ImplementationLoader.Wiring wiring = ImplementationLoader.wiring();
        ContractClauseUseCase useCase = wiring.implementationOf(ContractClauseUseCase.class);
        Optional<InMemoryContractRepository> repository = wiring.instanceOf(InMemoryContractRepository.class);
        assumeTrue(repository.isPresent(), "L'implémentation n'utilise pas l'adapter fourni");
        useCase.buildContractSummary(100000, 120, "LOW", false, false);
        useCase.buildContractSummary(-1, 120, "LOW", false, false);
        useCase.buildContractSummary(100000, 120, "NONE", false, false);
        assertEquals(1, repository.get().saved().size());
    }
}
