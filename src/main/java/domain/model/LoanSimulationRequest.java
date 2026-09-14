package domain.model;

public final class LoanSimulationRequest {
    private final LoanPrincipal principal;
    private final InterestRate rate;
    private final LoanTerm term;
    private final MonthlyIncome monthlyIncome;

    private LoanSimulationRequest(LoanPrincipal principal, InterestRate rate, LoanTerm term,
                                   MonthlyIncome monthlyIncome) {
        this.principal = principal;
        this.rate = rate;
        this.term = term;
        this.monthlyIncome = monthlyIncome;
    }

    // Input validity is checked before applicant eligibility, matching the legacy order.
    public static LoanSimulationRequest submit(double amount, double annualRate, int months,
                                                int applicantAgeYears, double monthlyIncome) {
        LoanPrincipal principal = new LoanPrincipal(amount);
        InterestRate rate = new InterestRate(annualRate);
        LoanTerm term = new LoanTerm(months);
        if (!new ApplicantAge(applicantAgeYears).isEligible()) {
            throw new LoanSimulationRejectedException(LoanSimulationRejection.APPLICANT_NOT_ELIGIBLE);
        }
        return new LoanSimulationRequest(principal, rate, term, new MonthlyIncome(monthlyIncome));
    }

    /**
     * Reproduces the legacy amortization formula as-is, including its behavior at a 0% rate
     * (division by zero producing NaN, which then fails the debt-ratio comparison silently
     * and is reported as APPROVED). Preserving this quirk is intentional: the exercise asks
     * for functional parity with the legacy code, not a bug fix — see SOLUTION.md.
     */
    public LoanSimulationOutcome simulate() {
        double monthlyRate = rate.monthlyRate();
        double monthlyPayment = (principal.value() * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -term.months()));
        double debtRatio = monthlyPayment / monthlyIncome.value();
        if (debtRatio > 0.35) {
            throw new LoanSimulationRejectedException(LoanSimulationRejection.DEBT_RATIO_TOO_HIGH);
        }
        return new LoanSimulationOutcome(monthlyPayment);
    }
}
