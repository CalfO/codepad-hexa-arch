package fr.cbtw.interview.domain.paymentprocessing;

/** Per debtor and execution date, in EUR equivalent; pending orders count toward the limit. */
public final class DailyLimit {
    private static final double LIMIT_IN_EUR = 1_000_000;

    private DailyLimit() {
    }

    public static ProcessingStatus statusFor(double alreadyCommittedInEur, double amountInEur) {
        return alreadyCommittedInEur + amountInEur > LIMIT_IN_EUR ? ProcessingStatus.PENDING_APPROVAL : ProcessingStatus.ACCEPTED;
    }
}
