package fr.cbtw.interview.domain.funds;

public final class FundsCheckRejectedException extends RuntimeException {
    private final FundsCheckRejection reason;

    public FundsCheckRejectedException(FundsCheckRejection reason) {
        super(reason.name());
        this.reason = reason;
    }

    public FundsCheckRejection reason() {
        return reason;
    }
}
