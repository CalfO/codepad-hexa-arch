package fr.cbtw.interview.domain.funds;

public enum FundsCheckRejection {
    UNKNOWN_ACCOUNT_STATUS,
    ACCOUNT_CLOSED,
    ACCOUNT_BLOCKED,
    INVALID_PAYMENT_AMOUNT,
    INVALID_OVERDRAFT,
    INSUFFICIENT_FUNDS
}
