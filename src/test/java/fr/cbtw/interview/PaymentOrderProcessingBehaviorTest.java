package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import fr.cbtw.interview.application.port.in.PaymentOrderProcessingUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Traitement des ordres de paiement")
class PaymentOrderProcessingBehaviorTest {
    private static final String FR = "FR7630001007941234567890185";
    private static final String DE = "DE89370400440532013000";
    private static final String GB = "GB29NWBK60161331926819";
    private static final LocalDate NEXT_MONDAY = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    private PaymentOrderProcessingUseCase loadCandidateImplementation() {
        return ImplementationLoader.findImplementationOf(PaymentOrderProcessingUseCase.class);
    }

    @Test
    @DisplayName("Un virement EUR entre deux pays SEPA part en SEPA, sans frais")
    void euroTransferWithinSepaZoneGoesThroughSepa() {
        assertEquals("ACCEPTED;scheme=SEPA;execution=" + NEXT_MONDAY + ";fees=0.00",
            loadCandidateImplementation().submit("INV-001", FR, DE, null, 1500, "EUR", NEXT_MONDAY, false));
    }

    @Test
    @DisplayName("Une référence déjà soumise est signalée comme doublon")
    void alreadySubmittedReferenceIsReportedAsDuplicate() {
        PaymentOrderProcessingUseCase useCase = loadCandidateImplementation();
        useCase.submit("INV-002", FR, DE, null, 1500, "EUR", NEXT_MONDAY, false);
        assertEquals("DUPLICATE: INV-002", useCase.submit("INV-002", FR, DE, null, 900, "EUR", NEXT_MONDAY, false));
    }

    @Test
    @DisplayName("Un paiement hors zone SEPA sans BIC est refusé")
    void paymentOutsideSepaZoneWithoutBicIsRejected() {
        assertEquals("REJECTED: BIC required for SWIFT payment",
            loadCandidateImplementation().submit("INV-003", FR, GB, null, 1500, "GBP", NEXT_MONDAY, false));
    }

    @Test
    @DisplayName("Au-delà d'un million d'euros engagés le même jour, le débiteur passe en attente d'approbation")
    void exceedingDailyLimitRequiresTreasuryApproval() {
        PaymentOrderProcessingUseCase useCase = loadCandidateImplementation();
        useCase.submit("TRF-1", FR, DE, null, 600000, "EUR", NEXT_MONDAY, false);
        assertEquals("PENDING_APPROVAL;scheme=SEPA;execution=" + NEXT_MONDAY + ";fees=0.00",
            useCase.submit("TRF-2", FR, DE, null, 500000, "EUR", NEXT_MONDAY, false));
    }
}
