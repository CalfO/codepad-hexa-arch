package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import fr.cbtw.interview.application.port.in.KycComplianceUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

public class KycComplianceBehaviorTest {
    private KycComplianceUseCase loadCandidateImplementation() {
        return ImplementationLoader.findImplementationOf(KycComplianceUseCase.class);
    }

    @Test
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
