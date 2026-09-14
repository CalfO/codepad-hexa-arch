package fr.cbtw.interview.application.port.out;

import fr.cbtw.interview.domain.model.LoanSimulationOutcome;

public interface LoanSimulationRepository {
    void save(LoanSimulationOutcome outcome);
}
