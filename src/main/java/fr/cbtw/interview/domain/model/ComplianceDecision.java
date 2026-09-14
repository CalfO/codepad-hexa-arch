package fr.cbtw.interview.domain.model;

public enum ComplianceDecision {
    MANUAL_REVIEW, APPROVED_WITH_MONITORING, APPROVED;

    public static ComplianceDecision fromRiskScore(int riskScore) {
        if (riskScore >= 50) {
            return MANUAL_REVIEW;
        }
        if (riskScore >= 20) {
            return APPROVED_WITH_MONITORING;
        }
        return APPROVED;
    }
}
