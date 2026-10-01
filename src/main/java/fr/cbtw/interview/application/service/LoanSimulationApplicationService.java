package fr.cbtw.interview.application.service;

import fr.cbtw.interview.application.port.in.LoanSimulationUseCase;
import fr.cbtw.interview.application.port.out.LoanSimulationRepository;
import fr.cbtw.interview.domain.loan.LoanSimulationOutcome;
import fr.cbtw.interview.domain.loan.LoanSimulationRejectedException;
import fr.cbtw.interview.domain.loan.LoanSimulationRequest;

public final class LoanSimulationApplicationService implements LoanSimulationUseCase {
    private final LoanSimulationRepository<LoanSimulationOutcome> repository;

    public LoanSimulationApplicationService(LoanSimulationRepository<LoanSimulationOutcome> repository) {
        this.repository = repository;
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
