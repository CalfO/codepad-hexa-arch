package infrastructure;

import application.port.out.LoanSimulationRepository;
import domain.model.LoanSimulationOutcome;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryLoanSimulationRepository implements LoanSimulationRepository {
    private final List<LoanSimulationOutcome> savedSimulations = new ArrayList<>();

    @Override
    public void save(LoanSimulationOutcome outcome) {
        savedSimulations.add(outcome);
    }
}
