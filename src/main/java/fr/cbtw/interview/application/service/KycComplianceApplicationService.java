package fr.cbtw.interview.application.service;

import java.time.Clock;
import java.time.LocalDate;

import fr.cbtw.interview.application.port.in.KycComplianceUseCase;
import fr.cbtw.interview.application.port.out.ComplianceCheckRepository;
import fr.cbtw.interview.domain.kyc.ComplianceOutcome;
import fr.cbtw.interview.domain.kyc.KycApplicant;
import fr.cbtw.interview.domain.kyc.KycRejectedException;

public final class KycComplianceApplicationService implements KycComplianceUseCase {
    private final ComplianceCheckRepository<ComplianceOutcome> repository;
    private final Clock clock;

    public KycComplianceApplicationService(ComplianceCheckRepository<ComplianceOutcome> repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public String checkCompliance(String documentType, LocalDate issueDate, LocalDate expiryDate,
                                   String fullName, String address, double declaredIncome,
                                   boolean isPoliticallyExposed) {
        try {
            KycApplicant applicant = KycApplicant.submit(LocalDate.now(clock), documentType, issueDate, expiryDate,
                fullName, address, declaredIncome, isPoliticallyExposed);
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
