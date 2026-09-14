package fr.cbtw.interview.domain.model;

public record LoanAmount(double value) {
    public LoanAmount {
        if (value <= 0) {
            throw new ContractRejectedException(ContractRejection.INVALID_PARAMETERS);
        }
    }

    public boolean exceeds(double threshold) {
        return value > threshold;
    }
}
