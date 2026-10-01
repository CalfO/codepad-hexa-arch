package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.FundsAvailabilityUseCase;
import fr.cbtw.interview.legacy.FundsAvailabilityService;
import fr.cbtw.interview.utils.ImplementationLoader;

/** Double run en miniature : même entrée, même sortie que le legacy, au caractère près. */
@DisplayName("Contrôle de provision — parité avec le legacy")
class FundsAvailabilityParityTest {
    private final FundsAvailabilityUseCase legacy = new FundsAvailabilityService();

    @ParameterizedTest(name = "[{index}] {0} solde={1} en-cours={2} découvert={3} paiement={4}")
    @CsvSource(nullValues = "NULL", value = {
        // statut, solde comptable, débits en attente, découvert autorisé, montant du paiement
        "ACTIVE,  10000,   1000,  2000,  4000",
        "ACTIVE,  10000,   0,     0,     9999.5",
        "ACTIVE,  1000,    0,     5000,  2000",
        "ACTIVE,  1000,    0,     5000,  5500",
        "ACTIVE,  0,       0,     10000, 9000",
        "ACTIVE,  -2000,   0,     10000, 3000",
        "ACTIVE,  -2000,   0,     10000, 7000",
        "ACTIVE,  1000,    500,   1000,  2000",
        "ACTIVE,  1000,    0,     0,     1000.5",
        "ACTIVE,  250000,  12000, 50000, 280000",
        "ACTIVE,  250000,  12000, 50000, 120000",
        "ACTIVE,  5000,    4500,  0,     400",
        "ACTIVE,  10000,   0,     2000,  0",
        "ACTIVE,  10000,   0,     2000,  -50",
        "ACTIVE,  10000,   0,     -1,    500",
        "ACTIVE,  10000,   0,     -1,    0",
        "CLOSED,  10000,   0,     0,     100",
        "BLOCKED, 10000,   0,     0,     100",
        "BLOCKED, 10000,   0,     -1,    -100",
        "NULL,    10000,   0,     0,     100",
        "DORMANT, 10000,   0,     0,     100",
        "active,  10000,   0,     0,     100",
        "'',      10000,   0,     0,     100",
        "ACTIVE,  3000.25, 0.75,  1500,  4000",
        "ACTIVE,  800,     100,   300,   950",
    })
    void producesExactlyTheSameResultAsLegacy(String status, double ledger, double pending, double overdraft,
                                               double amount) {
        FundsAvailabilityUseCase candidate = ImplementationLoader.findImplementationOf(FundsAvailabilityUseCase.class);
        assertEquals(legacy.checkFunds(status, ledger, pending, overdraft, amount),
            candidate.checkFunds(status, ledger, pending, overdraft, amount));
    }
}
