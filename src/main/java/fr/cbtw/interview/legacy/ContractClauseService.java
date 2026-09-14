package fr.cbtw.interview.legacy;

import java.util.ArrayList;
import java.util.List;

public class ContractClauseService {
    static List<String> savedContracts = new ArrayList<>();

    public String buildContractSummary(double loanAmount, int durationMonths, String riskProfile,
                                        boolean hasCoBorrower, boolean isFirstTimeBuyer) {
        if (loanAmount <= 0 || durationMonths <= 0) {
            return "ERROR: invalid contract parameters";
        }
        if (riskProfile == null ||
            (!riskProfile.equals("LOW") && !riskProfile.equals("MEDIUM") && !riskProfile.equals("HIGH"))) {
            return "ERROR: unknown risk profile";
        }

        List<String> clauses = new ArrayList<>();
        clauses.add("STANDARD_TERMS");

        if (loanAmount > 200000) {
            clauses.add("MORTGAGE_GUARANTEE_REQUIRED");
        }
        if (durationMonths > 240) {
            clauses.add("LONG_TERM_INSURANCE_CLAUSE");
        }
        if (riskProfile.equals("HIGH")) {
            clauses.add("ADDITIONAL_GUARANTOR_REQUIRED");
        }
        if (riskProfile.equals("HIGH") && !hasCoBorrower) {
            clauses.add("MANDATORY_LIFE_INSURANCE");
        }
        if (isFirstTimeBuyer && loanAmount <= 200000) {
            clauses.add("FIRST_TIME_BUYER_REDUCED_FEES");
        }
        if (hasCoBorrower) {
            clauses.add("JOINT_LIABILITY_CLAUSE");
        }

        String summary = String.join(",", clauses);
        savedContracts.add(summary);
        return "CLAUSES:" + summary;
    }
}
