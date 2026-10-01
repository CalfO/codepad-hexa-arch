package fr.cbtw.interview.infrastructure;

import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.out.PaymentOrderRepository;

public final class InMemoryPaymentOrderRepository<T> implements PaymentOrderRepository<T> {
    private final List<T> saved = new ArrayList<>();

    @Override
    public void save(T order) {
        saved.add(order);
    }

    public List<T> saved() {
        return List.copyOf(saved);
    }
}
