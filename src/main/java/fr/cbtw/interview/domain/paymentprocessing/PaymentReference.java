package fr.cbtw.interview.domain.paymentprocessing;

/** Client-side reference of an order; the idempotency key of the submission. */
public record PaymentReference(String value) {
    public PaymentReference {
        if (value == null || value.isBlank()) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.MISSING_REFERENCE);
        }
    }
}
