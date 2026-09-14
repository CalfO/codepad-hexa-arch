package domain.service;

import application.port.out.LoanSimulationRepository;
import domain.model.LoanSimulationOutcome;
import domain.model.LoanSimulationRejectedException;
import domain.model.LoanSimulationRequest;
import fr.cbtw.interview.application.port.in.LoanSimulationUseCase;
import infrastructure.InMemoryLoanSimulationRepository;

public final class LoanSimulationService implements LoanSimulationUseCase {
    private final LoanSimulationRepository repository;

    public LoanSimulationService(LoanSimulationRepository repository) {
        this.repository = repository;
    }

    // No-arg constructor so the test harness (ImplementationLoader) can instantiate this
    // class by reflection without external wiring. A real composition root should prefer
    // the constructor above with an injected adapter.
    public LoanSimulationService() {
        this(new InMemoryLoanSimulationRepository());
    }

    @Override
    public String simulate(double amount, double rate, int months, int applicantAge, double monthlyIncome) {
        try {
            LoanSimulationRequest request = LoanSimulationRequest.submit(amount, rate, months, applicantAge,
                monthlyIncome);
            LoanSimulationOutcome outcome = request.simulate();
            repository.save(outcome);
            return outcome.render();
        } catch (LoanSimulationRejectedException e) {
            return switch (e.reason()) {
                case INVALID_INPUT -> "ERROR: invalid input";
                case APPLICANT_NOT_ELIGIBLE -> "ERROR: applicant not eligible";
                case DEBT_RATIO_TOO_HIGH -> "REJECTED: debt ratio too high";
            };
        }
    }
}
