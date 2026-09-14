package application.port.out;

import domain.model.LoanSimulationOutcome;

public interface LoanSimulationRepository {
    void save(LoanSimulationOutcome outcome);
}
