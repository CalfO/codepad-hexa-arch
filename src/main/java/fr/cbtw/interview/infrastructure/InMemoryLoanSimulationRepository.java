package fr.cbtw.interview.infrastructure;

import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.out.LoanSimulationRepository;

public final class InMemoryLoanSimulationRepository<T> implements LoanSimulationRepository<T> {
    private final List<T> saved = new ArrayList<>();

    @Override
    public void save(T simulation) {
        saved.add(simulation);
    }

    public List<T> saved() {
        return List.copyOf(saved);
    }
}
