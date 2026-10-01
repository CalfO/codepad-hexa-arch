package fr.cbtw.interview.domain.payment;

public enum PaymentOrderRejection {
    MISSING_ACCOUNT,
    IDENTICAL_ACCOUNTS,
    INVALID_AMOUNT,
    TOO_MANY_DECIMALS,
    UNSUPPORTED_CURRENCY,
    URGENT_ONLY_IN_EUR,
    URGENT_AMOUNT_ABOVE_LIMIT,
    EXECUTION_DATE_IN_THE_PAST
}
