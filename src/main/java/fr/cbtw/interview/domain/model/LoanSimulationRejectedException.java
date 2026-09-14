package fr.cbtw.interview.domain.model;

public final class LoanSimulationRejectedException extends RuntimeException {
    private final LoanSimulationRejection reason;

    public LoanSimulationRejectedException(LoanSimulationRejection reason) {
        super(reason.name());
        this.reason = reason;
    }

    public LoanSimulationRejection reason() {
        return reason;
    }
}
