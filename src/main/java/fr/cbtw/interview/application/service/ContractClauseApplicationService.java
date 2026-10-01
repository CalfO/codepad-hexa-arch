package fr.cbtw.interview.application.service;

import fr.cbtw.interview.application.port.in.ContractClauseUseCase;
import fr.cbtw.interview.application.port.out.ContractRepository;
import fr.cbtw.interview.domain.contract.ContractApplication;
import fr.cbtw.interview.domain.contract.ContractRejectedException;
import fr.cbtw.interview.domain.contract.ContractSummary;

public final class ContractClauseApplicationService implements ContractClauseUseCase {
    private final ContractRepository<ContractSummary> repository;

    public ContractClauseApplicationService(ContractRepository<ContractSummary> repository) {
        this.repository = repository;
    }

    @Override
    public String buildContractSummary(double loanAmount, int durationMonths, String riskProfile,
                                        boolean hasCoBorrower, boolean isFirstTimeBuyer) {
        try {
            ContractApplication application = ContractApplication.submit(loanAmount, durationMonths, riskProfile,
                hasCoBorrower, isFirstTimeBuyer);
            ContractSummary summary = application.requiredClauses();
            repository.save(summary);
            return "CLAUSES:" + summary.render();
        } catch (ContractRejectedException e) {
            return switch (e.reason()) {
                case INVALID_PARAMETERS -> "ERROR: invalid contract parameters";
                case UNKNOWN_RISK_PROFILE -> "ERROR: unknown risk profile";
            };
        }
    }
}
