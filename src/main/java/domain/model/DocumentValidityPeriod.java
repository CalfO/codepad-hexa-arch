package domain.model;

import java.time.LocalDate;

public record DocumentValidityPeriod(LocalDate issueDate, LocalDate expiryDate) {
    // Expiry is checked before issue date, matching the legacy validation order.
    public DocumentValidityPeriod {
        if (expiryDate == null || expiryDate.isBefore(LocalDate.now())) {
            throw new KycRejectedException(KycRejection.EXPIRED_DOCUMENT);
        }
        if (issueDate == null || issueDate.isAfter(LocalDate.now())) {
            throw new KycRejectedException(KycRejection.INVALID_ISSUE_DATE);
        }
    }
}
