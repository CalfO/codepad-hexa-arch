package domain.model;

import java.util.ArrayList;
import java.util.List;

public final class ContractApplication {
    private final LoanAmount amount;
    private final ContractDuration duration;
    private final RiskProfile riskProfile;
    private final boolean hasCoBorrower;
    private final boolean isFirstTimeBuyer;

    private ContractApplication(LoanAmount amount, ContractDuration duration, RiskProfile riskProfile,
                                 boolean hasCoBorrower, boolean isFirstTimeBuyer) {
        this.amount = amount;
        this.duration = duration;
        this.riskProfile = riskProfile;
        this.hasCoBorrower = hasCoBorrower;
        this.isFirstTimeBuyer = isFirstTimeBuyer;
    }

    // Statement order mirrors the legacy service: amount/duration validated before risk profile.
    public static ContractApplication submit(double loanAmount, int durationMonths, String riskProfileCode,
                                              boolean hasCoBorrower, boolean isFirstTimeBuyer) {
        LoanAmount amount = new LoanAmount(loanAmount);
        ContractDuration duration = new ContractDuration(durationMonths);
        RiskProfile riskProfile = RiskProfile.fromCode(riskProfileCode);
        return new ContractApplication(amount, duration, riskProfile, hasCoBorrower, isFirstTimeBuyer);
    }

    public ContractSummary requiredClauses() {
        List<ContractClauseCode> clauses = new ArrayList<>();
        clauses.add(ContractClauseCode.STANDARD_TERMS);

        if (amount.exceeds(200_000)) {
            clauses.add(ContractClauseCode.MORTGAGE_GUARANTEE_REQUIRED);
        }
        if (duration.exceedsMonths(240)) {
            clauses.add(ContractClauseCode.LONG_TERM_INSURANCE_CLAUSE);
        }
        if (riskProfile == RiskProfile.HIGH) {
            clauses.add(ContractClauseCode.ADDITIONAL_GUARANTOR_REQUIRED);
        }
        if (riskProfile == RiskProfile.HIGH && !hasCoBorrower) {
            clauses.add(ContractClauseCode.MANDATORY_LIFE_INSURANCE);
        }
        if (isFirstTimeBuyer && !amount.exceeds(200_000)) {
            clauses.add(ContractClauseCode.FIRST_TIME_BUYER_REDUCED_FEES);
        }
        if (hasCoBorrower) {
            clauses.add(ContractClauseCode.JOINT_LIABILITY_CLAUSE);
        }
        return new ContractSummary(clauses);
    }
}
