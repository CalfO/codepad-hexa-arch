package application.port.out;

import domain.model.ComplianceOutcome;

public interface ComplianceCheckRepository {
    void save(ComplianceOutcome outcome);
}
