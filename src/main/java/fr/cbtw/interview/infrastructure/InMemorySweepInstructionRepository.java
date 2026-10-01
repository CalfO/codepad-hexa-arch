package fr.cbtw.interview.infrastructure;

import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.out.SweepInstructionRepository;

public final class InMemorySweepInstructionRepository<T> implements SweepInstructionRepository<T> {
    private final List<T> saved = new ArrayList<>();

    @Override
    public void save(T instruction) {
        saved.add(instruction);
    }

    public List<T> saved() {
        return List.copyOf(saved);
    }
}
