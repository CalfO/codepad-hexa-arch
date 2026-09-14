package fr.cbtw.interview.application.port.out;

import fr.cbtw.interview.domain.model.ComplianceOutcome;

public interface ComplianceCheckRepository {
    void save(ComplianceOutcome outcome);
}
