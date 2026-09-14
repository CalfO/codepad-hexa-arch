package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import fr.cbtw.interview.application.port.in.KycComplianceUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Conformité KYC")
public class KycComplianceBehaviorTest {
    private KycComplianceUseCase loadCandidateImplementation() {
        return ImplementationLoader.findImplementationOf(KycComplianceUseCase.class);
    }

    @Test
    @DisplayName("Un profil conforme à faible risque est approuvé")
    void approvesCompliantLowRiskProfile() {
        KycComplianceUseCase useCase = loadCandidateImplementation();
        String result = useCase.checkCompliance(
            "ID_CARD",
            LocalDate.now().minusYears(2),
            LocalDate.now().plusYears(3),
            "Jean Dupont",
            "12 rue de la Paix, Paris",
            2500,
            false
        );
        assertTrue(result.contains("APPROVED") && !result.contains("MANUAL_REVIEW"));
    }

    @Test
    @DisplayName("Une personne politiquement exposée est signalée pour revue manuelle")
    void flagsPoliticallyExposedPersonForManualReview() {
        KycComplianceUseCase useCase = loadCandidateImplementation();
        String result = useCase.checkCompliance(
            "PASSPORT",
            LocalDate.now().minusYears(1),
            LocalDate.now().plusYears(5),
            "Marie Curie",
            "1 rue du Radium, Lyon",
            5000,
            true
        );
        assertTrue(result.contains("MANUAL_REVIEW"));
    }

    @Test
    @DisplayName("Un document expiré est rejeté")
    void rejectsExpiredDocument() {
        KycComplianceUseCase useCase = loadCandidateImplementation();
        String result = useCase.checkCompliance(
            "ID_CARD",
            LocalDate.now().minusYears(10),
            LocalDate.now().minusDays(1),
            "Paul Martin",
            "5 avenue Foch, Marseille",
            3000,
            false
        );
        assertTrue(result.contains("REJECTED"));
    }

    // Previously untested: a missing document type must be rejected explicitly, before any
    // other field is even considered.
    @Test
    @DisplayName("Un type de document manquant est rejeté")
    void missingDocumentTypeIsRejected() {
        KycComplianceUseCase useCase = loadCandidateImplementation();
        String result = useCase.checkCompliance(
            "",
            LocalDate.now().minusYears(1),
            LocalDate.now().plusYears(1),
            "Jean Dupont",
            "12 rue de la Paix, Paris",
            2500,
            false
        );
        assertEquals("ERROR: missing document type", result);
    }

    // Previously untested boundary: a non-positive declared income alone (score 20, no PEP)
    // sits exactly on the monitoring threshold, not high enough for manual review.
    @Test
    @DisplayName("Un revenu déclaré nul déclenche une approbation sous surveillance (score au seuil de 20)")
    void zeroDeclaredIncomeTriggersApprovedWithMonitoring() {
        KycComplianceUseCase useCase = loadCandidateImplementation();
        String result = useCase.checkCompliance(
            "ID_CARD",
            LocalDate.now().minusYears(2),
            LocalDate.now().plusYears(3),
            "Jean Dupont",
            "12 rue de la Paix, Paris",
            0,
            false
        );
        assertEquals("STATUS:APPROVED_WITH_MONITORING;RISK:20", result);
    }
}
