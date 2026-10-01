package fr.cbtw.interview.domain.paymentprocessing;

public record Money(double amount, Currency currency) {
    public Money {
        checkAmount(amount);
    }

    // Legacy check kept as-is, including its floating-point artefact (19.99 is rejected) — see SOLUTION.md.
    // Exposed separately because the legacy validates the amount before it knows the currency.
    public static void checkAmount(double amount) {
        if (amount <= 0 || Math.round(amount * 100) != amount * 100) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.INVALID_AMOUNT);
        }
    }

    public double inEur() {
        return currency.toEur(amount);
    }
}
