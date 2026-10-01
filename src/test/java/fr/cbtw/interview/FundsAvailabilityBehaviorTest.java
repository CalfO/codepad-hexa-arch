package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import fr.cbtw.interview.application.port.in.FundsAvailabilityUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Contrôle de provision")
class FundsAvailabilityBehaviorTest {

    private FundsAvailabilityUseCase loadCandidateImplementation() {
        return ImplementationLoader.findImplementationOf(FundsAvailabilityUseCase.class);
    }

    @Test
    @DisplayName("Un paiement couvert par le solde est autorisé")
    void paymentCoveredByBalanceIsAuthorized() {
        assertEquals("AUTHORIZED: remaining=7000.0", loadCandidateImplementation()
            .checkFunds("ACTIVE", 10000, 1000, 2000, 4000));
    }

    @Test
    @DisplayName("Un paiement qui entame modérément le découvert autorisé est autorisé avec découvert")
    void paymentUsingPartOfOverdraftIsAuthorizedWithOverdraft() {
        assertEquals("AUTHORIZED_WITH_OVERDRAFT: remaining=4000.0", loadCandidateImplementation()
            .checkFunds("ACTIVE", 1000, 0, 5000, 2000));
    }

    @Test
    @DisplayName("Un paiement au-delà du disponible est refusé")
    void paymentAboveAvailableIsRejected() {
        assertEquals("REJECTED: insufficient funds", loadCandidateImplementation()
            .checkFunds("ACTIVE", 1000, 500, 1000, 2000));
    }

    @Test
    @DisplayName("Un compte bloqué ne peut pas être débité")
    void blockedAccountCannotBeDebited() {
        assertEquals("REJECTED: account blocked", loadCandidateImplementation()
            .checkFunds("BLOCKED", 10000, 0, 0, 100));
    }
}
