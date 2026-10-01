package fr.cbtw.interview.domain.funds;

/**
 * Position of an account at the time of the check. Pending debits are not validated: a negative value
 * (pending credits) increases the available amount, exactly as in the legacy.
 */
public record AccountPosition(double ledgerBalance, double pendingDebits, double authorizedOverdraft) {
    private static final double OVERDRAFT_APPROVAL_THRESHOLD = 0.8;

    public AccountPosition {
        if (authorizedOverdraft < 0) {
            throw new FundsCheckRejectedException(FundsCheckRejection.INVALID_OVERDRAFT);
        }
    }

    public double available() {
        return ledgerBalance - pendingDebits + authorizedOverdraft;
    }

    public FundsReservation reserve(double paymentAmount) {
        double available = available();
        if (paymentAmount > available) {
            throw new FundsCheckRejectedException(FundsCheckRejection.INSUFFICIENT_FUNDS);
        }
        double balanceAfterPayment = ledgerBalance - pendingDebits - paymentAmount;
        FundsDecision decision;
        if (balanceAfterPayment >= 0) {
            decision = FundsDecision.AUTHORIZED;
        } else {
            double overdraftUsage = -balanceAfterPayment / authorizedOverdraft;
            decision = overdraftUsage > OVERDRAFT_APPROVAL_THRESHOLD
                ? FundsDecision.PENDING_APPROVAL : FundsDecision.AUTHORIZED_WITH_OVERDRAFT;
        }
        return new FundsReservation(decision, paymentAmount, available - paymentAmount);
    }
}
