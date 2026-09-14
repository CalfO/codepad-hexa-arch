package fr.cbtw.interview.domain.service;

import fr.cbtw.interview.application.port.out.ComplianceCheckRepository;
import fr.cbtw.interview.domain.model.ComplianceOutcome;
import fr.cbtw.interview.domain.model.KycApplicant;
import fr.cbtw.interview.domain.model.KycRejectedException;
import fr.cbtw.interview.application.port.in.KycComplianceUseCase;
import fr.cbtw.interview.infrastructure.InMemoryComplianceCheckRepository;

import java.time.LocalDate;

public final class KycComplianceService implements KycComplianceUseCase {
    private final ComplianceCheckRepository repository;

    public KycComplianceService(ComplianceCheckRepository repository) {
        this.repository = repository;
    }

    // No-arg constructor so the test harness (ImplementationLoader) can instantiate this
    // class by reflection without external wiring. A real composition root should prefer
    // the constructor above with an injected adapter.
    public KycComplianceService() {
        this(new InMemoryComplianceCheckRepository());
    }

    @Override
    public String checkCompliance(String documentType, LocalDate issueDate, LocalDate expiryDate,
                                   String fullName, String address, double declaredIncome,
                                   boolean isPoliticallyExposed) {
        try {
            KycApplicant applicant = KycApplicant.submit(documentType, issueDate, expiryDate, fullName, address,
                declaredIncome, isPoliticallyExposed);
            ComplianceOutcome outcome = applicant.assessCompliance();
            repository.save(outcome);
            return outcome.render();
        } catch (KycRejectedException e) {
            return switch (e.reason()) {
                case MISSING_DOCUMENT_TYPE -> "ERROR: missing document type";
                case UNSUPPORTED_DOCUMENT_TYPE -> "ERROR: unsupported document type";
                case INCOMPLETE_PROFILE -> "REJECTED: incomplete profile";
                case EXPIRED_DOCUMENT -> "REJECTED: expired document";
                case INVALID_ISSUE_DATE -> "ERROR: invalid issue date";
            };
        }
    }
}
