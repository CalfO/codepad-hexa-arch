package fr.cbtw.interview.domain.paymentprocessing;

public final class PaymentOrderRejectedException extends RuntimeException {
    private final PaymentOrderRejection reason;

    public PaymentOrderRejectedException(PaymentOrderRejection reason) {
        super(reason.name());
        this.reason = reason;
    }

    public PaymentOrderRejection reason() {
        return reason;
    }
}
