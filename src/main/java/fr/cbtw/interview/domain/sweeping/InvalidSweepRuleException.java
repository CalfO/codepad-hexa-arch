package fr.cbtw.interview.domain.sweeping;

public final class InvalidSweepRuleException extends RuntimeException {
    private final SweepRuleRejection reason;

    public InvalidSweepRuleException(SweepRuleRejection reason) {
        super(reason.name());
        this.reason = reason;
    }

    public SweepRuleRejection reason() {
        return reason;
    }
}
