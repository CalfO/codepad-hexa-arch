package fr.cbtw.interview.domain.model;

public record InterestRate(double annualPercentage) {
    public InterestRate {
        if (annualPercentage < 0) {
            throw new LoanSimulationRejectedException(LoanSimulationRejection.INVALID_INPUT);
        }
    }

    public double monthlyRate() {
        return annualPercentage / 12 / 100;
    }
}
