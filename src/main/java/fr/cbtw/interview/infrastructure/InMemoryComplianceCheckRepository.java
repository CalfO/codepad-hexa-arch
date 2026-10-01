package fr.cbtw.interview.infrastructure;

import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.out.ComplianceCheckRepository;

public final class InMemoryComplianceCheckRepository<T> implements ComplianceCheckRepository<T> {
    private final List<T> saved = new ArrayList<>();

    @Override
    public void save(T check) {
        saved.add(check);
    }

    public List<T> saved() {
        return List.copyOf(saved);
    }
}
