package application.port.out;

import domain.model.ContractSummary;

public interface ContractRepository {
    void save(ContractSummary summary);
}
