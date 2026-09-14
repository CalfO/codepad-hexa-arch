package domain.model;

import java.time.LocalDate;

public final class KycApplicant {
    private final IdentityDocumentType documentType;
    private final ApplicantIdentity identity;
    private final DocumentValidityPeriod validityPeriod;
    private final DeclaredIncome declaredIncome;
    private final boolean politicallyExposed;

    private KycApplicant(IdentityDocumentType documentType, ApplicantIdentity identity,
                          DocumentValidityPeriod validityPeriod, DeclaredIncome declaredIncome,
                          boolean politicallyExposed) {
        this.documentType = documentType;
        this.identity = identity;
        this.validityPeriod = validityPeriod;
        this.declaredIncome = declaredIncome;
        this.politicallyExposed = politicallyExposed;
    }

    // Statement order mirrors the legacy service: document type, then identity completeness,
    // then document validity (expiry before issue date).
    public static KycApplicant submit(String documentTypeCode, LocalDate issueDate, LocalDate expiryDate,
                                       String fullName, String address, double declaredIncome,
                                       boolean politicallyExposed) {
        IdentityDocumentType documentType = IdentityDocumentType.fromCode(documentTypeCode);
        ApplicantIdentity identity = new ApplicantIdentity(fullName, address);
        DocumentValidityPeriod validityPeriod = new DocumentValidityPeriod(issueDate, expiryDate);
        DeclaredIncome income = new DeclaredIncome(declaredIncome);
        return new KycApplicant(documentType, identity, validityPeriod, income, politicallyExposed);
    }

    // Risk weights (50/20/10) and the monitoring/manual-review thresholds (20/50) are the
    // legacy scoring rules, preserved as-is.
    public ComplianceOutcome assessCompliance() {
        int riskScore = 0;
        if (politicallyExposed) {
            riskScore += 50;
        }
        if (declaredIncome.isNonPositive()) {
            riskScore += 20;
        }
        if (declaredIncome.isLowPositiveIncome(1000)) {
            riskScore += 10;
        }
        return new ComplianceOutcome(ComplianceDecision.fromRiskScore(riskScore), riskScore);
    }
}
