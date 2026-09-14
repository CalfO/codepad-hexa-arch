package fr.cbtw.interview.application.port.out;

import fr.cbtw.interview.domain.model.ContractSummary;

public interface ContractRepository {
    void save(ContractSummary summary);
}
