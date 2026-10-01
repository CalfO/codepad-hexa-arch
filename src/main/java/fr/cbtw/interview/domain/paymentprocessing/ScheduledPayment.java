package fr.cbtw.interview.domain.paymentprocessing;

import java.time.LocalDate;

/** A valid order with its scheme, execution date and fees — not yet checked against the daily limit. */
public record ScheduledPayment(PaymentOrder order, PaymentScheme scheme, LocalDate executionDate, double fees) {
    public ProcessedPaymentOrder withStatus(ProcessingStatus status) {
        return new ProcessedPaymentOrder(order.reference(), order.debtor(), order.creditor(), order.money().inEur(),
            scheme, executionDate, fees, status);
    }
}
