package fr.cbtw.interview.infrastructure.paymentprocessing;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.out.paymentprocessing.PaymentOrderJournal;
import fr.cbtw.interview.domain.paymentprocessing.Iban;
import fr.cbtw.interview.domain.paymentprocessing.PaymentReference;
import fr.cbtw.interview.domain.paymentprocessing.ProcessedPaymentOrder;

public final class InMemoryPaymentOrderJournal implements PaymentOrderJournal {
    // Insertion order matters: the committed amount is a floating-point sum, summed in the legacy's order.
    private final List<ProcessedPaymentOrder> orders = new ArrayList<>();

    @Override
    public boolean contains(PaymentReference reference) {
        return orders.stream().anyMatch(o -> o.reference().equals(reference));
    }

    @Override
    public double committedAmountInEur(Iban debtor, LocalDate executionDate) {
        double committed = 0;
        for (ProcessedPaymentOrder order : orders) {
            if (order.debtor().equals(debtor) && order.executionDate().equals(executionDate)) {
                committed += order.amountInEur();
            }
        }
        return committed;
    }

    @Override
    public void record(ProcessedPaymentOrder order) {
        orders.add(order);
    }
}
