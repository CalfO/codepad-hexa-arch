package fr.cbtw.interview.domain.model;

public record LoanPrincipal(double value) {
    public LoanPrincipal {
        if (value <= 0) {
            throw new LoanSimulationRejectedException(LoanSimulationRejection.INVALID_INPUT);
        }
    }
}
