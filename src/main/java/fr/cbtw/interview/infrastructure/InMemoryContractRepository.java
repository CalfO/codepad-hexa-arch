package fr.cbtw.interview.infrastructure;

import fr.cbtw.interview.application.port.out.ContractRepository;
import fr.cbtw.interview.domain.model.ContractSummary;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryContractRepository implements ContractRepository {
    private final List<ContractSummary> savedContracts = new ArrayList<>();

    @Override
    public void save(ContractSummary summary) {
        savedContracts.add(summary);
    }
}
