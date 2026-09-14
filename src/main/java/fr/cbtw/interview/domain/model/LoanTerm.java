package fr.cbtw.interview.domain.model;

public record LoanTerm(int months) {
    public LoanTerm {
        if (months <= 0) {
            throw new LoanSimulationRejectedException(LoanSimulationRejection.INVALID_INPUT);
        }
    }
}
