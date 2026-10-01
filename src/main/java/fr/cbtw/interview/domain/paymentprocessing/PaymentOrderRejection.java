package fr.cbtw.interview.domain.paymentprocessing;

public enum PaymentOrderRejection {
    MISSING_REFERENCE,
    INVALID_DEBTOR_IBAN,
    INVALID_CREDITOR_IBAN,
    IDENTICAL_ACCOUNTS,
    INVALID_AMOUNT,
    UNSUPPORTED_CURRENCY,
    BIC_REQUIRED,
    EXECUTION_DATE_IN_THE_PAST
}
