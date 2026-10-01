package fr.cbtw.interview.domain.payment;

public record AccountIdentifier(String value) {
    public AccountIdentifier {
        if (value == null || value.isBlank()) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.MISSING_ACCOUNT);
        }
    }
}
