package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("STATUS:APPROVED;RISK:0", result);
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
        assertEquals("STATUS:MANUAL_REVIEW;RISK:50", result);
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
        assertEquals("REJECTED: expired document", result);
    }
}
