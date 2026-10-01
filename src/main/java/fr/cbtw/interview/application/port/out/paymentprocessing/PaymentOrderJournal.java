package fr.cbtw.interview.application.port.out.paymentprocessing;

import java.time.LocalDate;

import fr.cbtw.interview.domain.paymentprocessing.Iban;
import fr.cbtw.interview.domain.paymentprocessing.PaymentReference;
import fr.cbtw.interview.domain.paymentprocessing.ProcessedPaymentOrder;

/** Orders already submitted: idempotency (reference) and daily exposure (debtor, execution date). */
public interface PaymentOrderJournal {
    boolean contains(PaymentReference reference);

    /** EUR equivalent already committed by this debtor for this execution date, pending orders included. */
    double committedAmountInEur(Iban debtor, LocalDate executionDate);

    void record(ProcessedPaymentOrder order);
}
