package domain.model;

public record ContractDuration(int months) {
    public ContractDuration {
        if (months <= 0) {
            throw new ContractRejectedException(ContractRejection.INVALID_PARAMETERS);
        }
    }

    public boolean exceedsMonths(int threshold) {
        return months > threshold;
    }
}
