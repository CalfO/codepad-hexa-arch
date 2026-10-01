package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.CashSweepingUseCase;
import fr.cbtw.interview.legacy.CashSweepingService;
import fr.cbtw.interview.utils.ImplementationLoader;

/** Double run en miniature : même entrée, même sortie que le legacy, au caractère près. */
@DisplayName("Règle de sweeping — parité avec le legacy")
class CashSweepingParityTest {
    private final CashSweepingUseCase legacy = new CashSweepingService();

    @ParameterizedTest(name = "[{index}] {0} solde={1} cible={2} minimum={3} centralisateur={4}")
    @CsvSource(nullValues = "NULL", value = {
        // type, solde du sous-compte, solde cible, mouvement minimum, disponible du compte centralisateur
        "ZERO_BALANCING,   12500.40,  0,     100,  500000",
        "ZERO_BALANCING,   -8000,     0,     100,  500000",
        "ZERO_BALANCING,   -8000,     0,     100,  3000",
        "ZERO_BALANCING,   -8000,     0,     100,  0",
        "ZERO_BALANCING,   50,        0,     100,  500000",
        "ZERO_BALANCING,   1234.567,  0,     1,    500000",
        "ZERO_BALANCING,   12500,     -500,  100,  500000",
        "TARGET_BALANCING, 2000,      5000,  100,  500000",
        "TARGET_BALANCING, 9000,      5000,  100,  500000",
        "TARGET_BALANCING, 5050,      5000,  100,  500000",
        "TARGET_BALANCING, -2500.75,  1000,  0,    500000",
        "TARGET_BALANCING, 2000,      5000,  100,  1500.5",
        "TARGET_BALANCING, 2000,      5000,  100,  -10",
        "TARGET_BALANCING, 2000,      -1,    100,  500000",
        "THRESHOLD,        25000,     20000, 500,  500000",
        "THRESHOLD,        15000,     20000, 500,  500000",
        "THRESHOLD,        20300,     20000, 500,  500000",
        "THRESHOLD,        -5000,     0,     10,   500000",
        "THRESHOLD,        25000,     -1,    500,  500000",
        "NULL,             12500,     0,     100,  500000",
        "'',               12500,     0,     100,  500000",
        "NOTIONAL_POOLING, 12500,     0,     100,  500000",
        "zero_balancing,   12500,     0,     100,  500000",
        "ZERO_BALANCING,   12500,     0,     -1,   500000",
        "TARGET_BALANCING, 4999.994,  5000,  0.5,  500000",
    })
    void producesExactlyTheSameResultAsLegacy(String sweepType, double balance, double target, double minimum,
                                               double master) {
        CashSweepingUseCase candidate = ImplementationLoader.findImplementationOf(CashSweepingUseCase.class);
        assertEquals(legacy.computeSweep(sweepType, balance, target, minimum, master),
            candidate.computeSweep(sweepType, balance, target, minimum, master));
    }
}
