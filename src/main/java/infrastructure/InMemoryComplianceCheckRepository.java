package infrastructure;

import application.port.out.ComplianceCheckRepository;
import domain.model.ComplianceOutcome;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryComplianceCheckRepository implements ComplianceCheckRepository {
    private final List<ComplianceOutcome> savedChecks = new ArrayList<>();

    @Override
    public void save(ComplianceOutcome outcome) {
        savedChecks.add(outcome);
    }
}
