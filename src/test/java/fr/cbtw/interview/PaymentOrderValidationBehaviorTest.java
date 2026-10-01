package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import fr.cbtw.interview.application.port.in.PaymentOrderValidationUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Validation d'un ordre de paiement")
class PaymentOrderValidationBehaviorTest {
    private static final LocalDate NEXT_SATURDAY = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
    private static final LocalDate MONDAY_AFTER = NEXT_SATURDAY.plusDays(2);

    private PaymentOrderValidationUseCase loadCandidateImplementation() {
        return ImplementationLoader.findImplementationOf(PaymentOrderValidationUseCase.class);
    }

    @Test
    @DisplayName("Un virement standard en EUR un jour ouvré est accepté à la date demandée")
    void acceptsStandardEuroTransferOnBusinessDay() {
        String result = loadCandidateImplementation()
            .validate("FR7630001007941234567890185", "DE89370400440532013000", 2500, "EUR", MONDAY_AFTER, false);
        assertEquals("ACCEPTED: execution=" + MONDAY_AFTER + ";channel=STANDARD", result);
    }

    @Test
    @DisplayName("Un virement standard demandé un samedi est reporté au lundi")
    void standardTransferRequestedOnSaturdayRollsToMonday() {
        String result = loadCandidateImplementation()
            .validate("FR7630001007941234567890185", "DE89370400440532013000", 2500, "EUR", NEXT_SATURDAY, false);
        assertEquals("ACCEPTED: execution=" + MONDAY_AFTER + ";channel=STANDARD", result);
    }

    @Test
    @DisplayName("Un virement urgent en devise autre que l'EUR est refusé")
    void urgentTransferOutsideEuroIsRejected() {
        String result = loadCandidateImplementation()
            .validate("FR7630001007941234567890185", "GB29NWBK60161331926819", 2500, "GBP", MONDAY_AFTER, true);
        assertEquals("REJECTED: urgent payment only available in EUR", result);
    }

    @Test
    @DisplayName("Un ordre dont le débiteur et le créancier sont identiques est refusé")
    void identicalDebtorAndCreditorIsRejected() {
        String result = loadCandidateImplementation()
            .validate("FR7630001007941234567890185", "FR7630001007941234567890185", 2500, "EUR", MONDAY_AFTER, false);
        assertEquals("REJECTED: debtor and creditor accounts are identical", result);
    }
}
