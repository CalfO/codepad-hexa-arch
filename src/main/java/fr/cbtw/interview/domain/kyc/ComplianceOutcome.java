package fr.cbtw.interview.domain.kyc;

public record ComplianceOutcome(ComplianceDecision decision, int riskScore) {
    public String render() {
        return "STATUS:" + decision + ";RISK:" + riskScore;
    }
}
