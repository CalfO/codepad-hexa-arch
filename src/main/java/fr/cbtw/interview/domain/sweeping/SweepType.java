package fr.cbtw.interview.domain.sweeping;

import java.util.Arrays;

public enum SweepType {
    ZERO_BALANCING, TARGET_BALANCING, THRESHOLD;

    public static SweepType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidSweepRuleException(SweepRuleRejection.MISSING_SWEEP_TYPE);
        }
        return Arrays.stream(values()).filter(t -> t.name().equals(code)).findFirst()
            .orElseThrow(() -> new InvalidSweepRuleException(SweepRuleRejection.UNKNOWN_SWEEP_TYPE));
    }
}
