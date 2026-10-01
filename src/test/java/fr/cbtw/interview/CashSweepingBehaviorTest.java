package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import fr.cbtw.interview.application.port.in.CashSweepingUseCase;
import fr.cbtw.interview.utils.ImplementationLoader;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Règle de sweeping")
class CashSweepingBehaviorTest {

    private CashSweepingUseCase loadCandidateImplementation() {
        return ImplementationLoader.findImplementationOf(CashSweepingUseCase.class);
    }

    @Test
    @DisplayName("En zero balancing, un solde créditeur est remonté intégralement vers le compte centralisateur")
    void zeroBalancingSweepsPositiveBalanceToMaster() {
        assertEquals("SWEEP: TO_MASTER=12500.4", loadCandidateImplementation()
            .computeSweep("ZERO_BALANCING", 12500.40, 0, 100, 500000));
    }

    @Test
    @DisplayName("En target balancing, un solde sous la cible est complété par le compte centralisateur")
    void targetBalancingFundsSubAccountFromMaster() {
        assertEquals("SWEEP: FROM_MASTER=3000.0", loadCandidateImplementation()
            .computeSweep("TARGET_BALANCING", 2000, 5000, 100, 500000));
    }

    @Test
    @DisplayName("Un mouvement inférieur au montant minimum n'est pas déclenché")
    void transferBelowMinimumIsNotTriggered() {
        assertEquals("NO_SWEEP: below minimum transfer", loadCandidateImplementation()
            .computeSweep("TARGET_BALANCING", 5050, 5000, 100, 500000));
    }

    @Test
    @DisplayName("Un type de sweeping inconnu est refusé")
    void unknownSweepTypeIsRejected() {
        assertEquals("ERROR: unknown sweep type", loadCandidateImplementation()
            .computeSweep("NOTIONAL_POOLING", 12500, 0, 100, 500000));
    }
}
