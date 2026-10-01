package fr.cbtw.interview.domain.payment;

public record PaymentAmount(double value) {
    public PaymentAmount {
        if (value <= 0) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.INVALID_AMOUNT);
        }
        // Legacy check kept as-is: in binary floating point, 19.99 * 100 is 1998.9999999999998, so
        // perfectly valid amounts (19.99, 1.15, 0.29...) are rejected. Reproduced for parity; fixing it
        // (BigDecimal, or amounts in cents) is a business-visible change to agree on first — see SOLUTION.md.
        if (Math.round(value * 100) != value * 100) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.TOO_MANY_DECIMALS);
        }
    }

    public boolean exceeds(double limit) {
        return value > limit;
    }
}
