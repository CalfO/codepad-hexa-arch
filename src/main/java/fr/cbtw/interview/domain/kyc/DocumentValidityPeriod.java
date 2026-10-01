package fr.cbtw.interview.domain.kyc;

import java.time.LocalDate;

public record DocumentValidityPeriod(LocalDate issueDate, LocalDate expiryDate) {
    public DocumentValidityPeriod {
        if (expiryDate == null) {
            throw new KycRejectedException(KycRejection.EXPIRED_DOCUMENT);
        }
        if (issueDate == null) {
            throw new KycRejectedException(KycRejection.INVALID_ISSUE_DATE);
        }
    }

    // "today" is passed in rather than read from LocalDate.now(): the domain stays deterministic
    // and the application service decides where the current date comes from (an injected Clock).
    // Expiry is checked before issue date, matching the legacy validation order.
    public static DocumentValidityPeriod checkedOn(LocalDate today, LocalDate issueDate, LocalDate expiryDate) {
        if (expiryDate == null || expiryDate.isBefore(today)) {
            throw new KycRejectedException(KycRejection.EXPIRED_DOCUMENT);
        }
        if (issueDate == null || issueDate.isAfter(today)) {
            throw new KycRejectedException(KycRejection.INVALID_ISSUE_DATE);
        }
        return new DocumentValidityPeriod(issueDate, expiryDate);
    }
}
