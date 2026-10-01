package fr.cbtw.interview.domain.funds;

public final class FundsCheck {
    private FundsCheck() {
    }

    // Statement order mirrors the legacy service: account status, then payment amount, then overdraft.
    public static FundsReservation reserve(String accountStatus, double ledgerBalance, double pendingDebits,
                                           double authorizedOverdraft, double paymentAmount) {
        AccountStatus.fromCode(accountStatus).ensureCanBeDebited();
        if (paymentAmount <= 0) {
            throw new FundsCheckRejectedException(FundsCheckRejection.INVALID_PAYMENT_AMOUNT);
        }
        return new AccountPosition(ledgerBalance, pendingDebits, authorizedOverdraft).reserve(paymentAmount);
    }
}
