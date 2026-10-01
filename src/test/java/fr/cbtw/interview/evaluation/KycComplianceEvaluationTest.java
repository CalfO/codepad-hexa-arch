package fr.cbtw.interview.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.KycComplianceUseCase;
import fr.cbtw.interview.infrastructure.InMemoryComplianceCheckRepository;
import fr.cbtw.interview.legacy.KycComplianceService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Tests de l'évaluateur — NE PAS livrer dans un pad candidat (cf. NOTICE.md).
 * À copier dans le pad après la session pour mesurer ce que les tests fournis ne couvrent pas.
 */
@DisplayName("[Évaluation] Conformité KYC")
class KycComplianceEvaluationTest {
    private final KycComplianceUseCase legacy = new KycComplianceService();

    private static KycComplianceUseCase candidate() {
        return ImplementationLoader.findImplementationOf(KycComplianceUseCase.class);
    }

    @Test
    @DisplayName("Un type de document manquant est rejeté avant tout autre contrôle")
    void missingDocumentTypeIsRejected() {
        assertEquals("ERROR: missing document type", candidate().checkCompliance("",
            LocalDate.now().minusYears(1), LocalDate.now().plusYears(1), "Jean Dupont", "12 rue de la Paix", 2500, false));
    }

    @Test
    @DisplayName("Un revenu nul seul donne un score de 20, pile au seuil de surveillance")
    void zeroDeclaredIncomeTriggersApprovedWithMonitoring() {
        assertEquals("STATUS:APPROVED_WITH_MONITORING;RISK:20", candidate().checkCompliance("ID_CARD",
            LocalDate.now().minusYears(2), LocalDate.now().plusYears(3), "Jean Dupont", "12 rue de la Paix", 0, false));
    }

    @ParameterizedTest(name = "[{index}] {0} émis J{1} expire J{2} nom=[{3}] revenu={5} PPE={6}")
    @CsvSource(nullValues = "NULL", value = {
        "ID_CARD,  -730, 0,    Jean Dupont, 12 rue X, 2500,   false", // expire aujourd'hui : encore valide
        "ID_CARD,  0,    100,  Jean Dupont, 12 rue X, 2500,   false", // émis aujourd'hui : valide
        "ID_CARD,  NULL, 100,  Jean Dupont, 12 rue X, 2500,   false", // date d'émission absente
        "ID_CARD,  10,   -10,  Jean Dupont, 12 rue X, 2500,   false", // expiré ET émis dans le futur : l'expiration gagne
        "passport, -730, 100,  Jean Dupont, 12 rue X, 2500,   false", // casse
        "'  ',     -730, 100,  Jean Dupont, 12 rue X, 2500,   false", // type blanc
        "ID_CARD,  -730, 100,  '   ',       12 rue X, 2500,   false", // nom blanc
        "ID_CARD,  -730, 100,  Jean Dupont, 12 rue X, 1000,   false", // borne 1000 exclue du malus
        "ID_CARD,  -730, 100,  Jean Dupont, 12 rue X, 999.99, false",
        "ID_CARD,  -730, 100,  Jean Dupont, 12 rue X, -50,    false", // revenu négatif
        "ID_CARD,  -730, 100,  Jean Dupont, 12 rue X, 0,      true",  // PPE + revenu nul = 70
        "ID_CARD,  -730, 100,  Jean Dupont, 12 rue X, 500,    true",  // PPE + petit revenu = 60
    })
    void edgeCasesMatchLegacy(String type, Integer issueInDays, Integer expiryInDays, String name, String address,
                              double income, boolean pep) {
        LocalDate issue = issueInDays == null ? null : LocalDate.now().plusDays(issueInDays);
        LocalDate expiry = expiryInDays == null ? null : LocalDate.now().plusDays(expiryInDays);
        assertEquals(legacy.checkCompliance(type, issue, expiry, name, address, income, pep),
            candidate().checkCompliance(type, issue, expiry, name, address, income, pep));
    }

    @Test
    @DisplayName("Bonus : avec une horloge injectée, la validité du document se juge à la date de l'horloge")
    void documentValidityFollowsInjectedClock() {
        Clock clock = Clock.fixed(ZonedDateTime.of(2030, 6, 15, 10, 0, 0, 0, ZoneId.of("Europe/Paris")).toInstant(),
            ZoneId.of("Europe/Paris"));
        ImplementationLoader.Wiring wiring = ImplementationLoader.wiring().provide(Clock.class, clock);
        KycComplianceUseCase useCase = wiring.implementationOf(KycComplianceUseCase.class);
        assumeTrue(wiring.hasInjected(Clock.class), "Bonus non réalisé : l'implémentation ne reçoit pas de Clock");
        assertEquals("STATUS:APPROVED;RISK:0", useCase.checkCompliance("PASSPORT", LocalDate.of(2025, 1, 1),
            LocalDate.of(2030, 6, 15), "Jean Dupont", "12 rue X", 2500, false));
        assertEquals("REJECTED: expired document", useCase.checkCompliance("PASSPORT", LocalDate.of(2025, 1, 1),
            LocalDate.of(2030, 6, 14), "Jean Dupont", "12 rue X", 2500, false));
        assertEquals("ERROR: invalid issue date", useCase.checkCompliance("PASSPORT", LocalDate.of(2030, 6, 16),
            LocalDate.of(2035, 1, 1), "Jean Dupont", "12 rue X", 2500, false));
    }

    @Test
    @DisplayName("Seuls les contrôles aboutis sont enregistrés (si l'adapter fourni est utilisé)")
    void onlyCompletedChecksAreRecorded() {
        ImplementationLoader.Wiring wiring = ImplementationLoader.wiring();
        KycComplianceUseCase useCase = wiring.implementationOf(KycComplianceUseCase.class);
        Optional<InMemoryComplianceCheckRepository> repository = wiring.instanceOf(InMemoryComplianceCheckRepository.class);
        assumeTrue(repository.isPresent(), "L'implémentation n'utilise pas l'adapter fourni");
        useCase.checkCompliance("ID_CARD", LocalDate.now().minusYears(1), LocalDate.now().plusYears(1), "A B", "X", 2500, true);
        useCase.checkCompliance("ID_CARD", LocalDate.now().minusYears(1), LocalDate.now().minusDays(1), "A B", "X", 2500, false);
        useCase.checkCompliance("VISA", LocalDate.now().minusYears(1), LocalDate.now().plusYears(1), "A B", "X", 2500, false);
        assertEquals(1, repository.get().saved().size());
    }
}
