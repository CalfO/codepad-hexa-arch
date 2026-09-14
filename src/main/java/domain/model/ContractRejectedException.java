package domain.model;

public final class ContractRejectedException extends RuntimeException {
    private final ContractRejection reason;

    public ContractRejectedException(ContractRejection reason) {
        super(reason.name());
        this.reason = reason;
    }

    public ContractRejection reason() {
        return reason;
    }
}
