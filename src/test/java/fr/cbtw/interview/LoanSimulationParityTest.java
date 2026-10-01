package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.LoanSimulationUseCase;
import fr.cbtw.interview.legacy.LoanSimulationService;
import fr.cbtw.interview.utils.ImplementationLoader;

/** Double run en miniature : même entrée, même sortie que le legacy, au caractère près. */
@DisplayName("Simulation de prêt — parité avec le legacy")
class LoanSimulationParityTest {
    private final LoanSimulationUseCase legacy = new LoanSimulationService();

    @ParameterizedTest(name = "[{index}] montant={0} taux={1} durée={2} âge={3} revenu={4}")
    @CsvSource({
        // montant, taux annuel %, durée (mois), âge, revenu mensuel
        "150000, 3.5,  240, 35, 3000",
        "200000, 4.1,  300, 42, 4500",
        "50000,  2.9,  60,  25, 2200",
        "10000,  5.0,  12,  60, 1800",
        "320000, 3.2,  300, 38, 6000",
        "75000,  1.25, 180, 55, 1500",
        "999.5,  7.75, 6,   30, 900",
        "120000, 3.8,  360, 45, 2500",
        "400000, 4.5,  240, 35, 1500",
        "300000, 6.0,  120, 50, 3000",
        "250000, 3.5,  60,  40, 5000",
        "0,      3.5,  240, 35, 3000",
        "-1,     3.5,  240, 35, 3000",
        "150000, -0.5, 240, 35, 3000",
        "150000, 3.5,  0,   35, 3000",
        "150000, 3.5,  -12, 35, 3000",
        "150000, 3.5,  240, 17, 3000",
        "150000, 3.5,  240, 16, 3000",
        "150000, 3.5,  240, 76, 3000",
        "150000, 3.5,  240, 90, 3000",
        "-1,     3.5,  240, 12, 3000",
        "150000, 3.5,  0,   80, 3000",
        "80000,  3.0,  84,  67, 2000",
        "30000,  9.9,  48,  22, 950",
        "500000, 2.5,  300, 48, 8000",
    })
    void producesExactlyTheSameResultAsLegacy(double amount, double rate, int months, int age, double income) {
        LoanSimulationUseCase candidate = ImplementationLoader.findImplementationOf(LoanSimulationUseCase.class);
        assertEquals(legacy.simulate(amount, rate, months, age, income),
            candidate.simulate(amount, rate, months, age, income));
    }
}
