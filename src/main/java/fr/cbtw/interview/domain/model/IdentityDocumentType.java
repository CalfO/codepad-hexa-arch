package fr.cbtw.interview.domain.model;

import java.util.Optional;

public enum IdentityDocumentType {
    PASSPORT, ID_CARD, RESIDENCE_PERMIT;

    public static IdentityDocumentType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new KycRejectedException(KycRejection.MISSING_DOCUMENT_TYPE);
        }
        return parse(code).orElseThrow(() -> new KycRejectedException(KycRejection.UNSUPPORTED_DOCUMENT_TYPE));
    }

    private static Optional<IdentityDocumentType> parse(String code) {
        try {
            return Optional.of(IdentityDocumentType.valueOf(code));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
