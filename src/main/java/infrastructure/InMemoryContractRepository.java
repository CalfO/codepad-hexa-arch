package infrastructure;

import application.port.out.ContractRepository;
import domain.model.ContractSummary;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryContractRepository implements ContractRepository {
    private final List<ContractSummary> savedContracts = new ArrayList<>();

    @Override
    public void save(ContractSummary summary) {
        savedContracts.add(summary);
    }
}
