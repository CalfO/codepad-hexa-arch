package fr.cbtw.interview.infrastructure;

import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.out.FundsReservationRepository;

public final class InMemoryFundsReservationRepository<T> implements FundsReservationRepository<T> {
    private final List<T> saved = new ArrayList<>();

    @Override
    public void save(T reservation) {
        saved.add(reservation);
    }

    public List<T> saved() {
        return List.copyOf(saved);
    }
}
