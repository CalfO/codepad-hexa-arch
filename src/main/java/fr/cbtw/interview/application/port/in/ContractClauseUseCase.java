package fr.cbtw.interview.application.port.in;

public interface ContractClauseUseCase {
    String buildContractSummary(double loanAmount, int durationMonths, String riskProfile,
                                 boolean hasCoBorrower, boolean isFirstTimeBuyer);
}
