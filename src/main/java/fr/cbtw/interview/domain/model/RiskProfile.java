package fr.cbtw.interview.domain.model;

import java.util.Optional;

public enum RiskProfile {
    LOW, MEDIUM, HIGH;

    public static RiskProfile fromCode(String code) {
        return parse(code).orElseThrow(() -> new ContractRejectedException(ContractRejection.UNKNOWN_RISK_PROFILE));
    }

    private static Optional<RiskProfile> parse(String code) {
        if (code == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(RiskProfile.valueOf(code));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
