package fr.cbtw.interview.domain.model;

public record ComplianceOutcome(ComplianceDecision decision, int riskScore) {
    public String render() {
        return "STATUS:" + decision + ";RISK:" + riskScore;
    }
}
