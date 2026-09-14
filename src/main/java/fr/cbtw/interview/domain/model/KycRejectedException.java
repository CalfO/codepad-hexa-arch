package fr.cbtw.interview.domain.model;

public final class KycRejectedException extends RuntimeException {
    private final KycRejection reason;

    public KycRejectedException(KycRejection reason) {
        super(reason.name());
        this.reason = reason;
    }

    public KycRejection reason() {
        return reason;
    }
}
