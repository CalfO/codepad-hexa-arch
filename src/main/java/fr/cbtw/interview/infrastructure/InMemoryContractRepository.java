package fr.cbtw.interview.infrastructure;

import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.out.ContractRepository;

public final class InMemoryContractRepository<T> implements ContractRepository<T> {
    private final List<T> saved = new ArrayList<>();

    @Override
    public void save(T contract) {
        saved.add(contract);
    }

    public List<T> saved() {
        return List.copyOf(saved);
    }
}
