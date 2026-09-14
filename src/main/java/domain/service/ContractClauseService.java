package domain.service;

import application.port.out.ContractRepository;
import domain.model.ContractApplication;
import domain.model.ContractRejectedException;
import domain.model.ContractSummary;
import fr.cbtw.interview.application.port.in.ContractClauseUseCase;
import infrastructure.InMemoryContractRepository;

public final class ContractClauseService implements ContractClauseUseCase {
    private final ContractRepository repository;

    public ContractClauseService(ContractRepository repository) {
        this.repository = repository;
    }

    // No-arg constructor so the test harness (ImplementationLoader) can instantiate this
    // class by reflection without external wiring. A real composition root should prefer
    // the constructor above with an injected adapter.
    public ContractClauseService() {
        this(new InMemoryContractRepository());
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
