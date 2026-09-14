package fr.cbtw.interview.domain.model;

import java.util.List;
import java.util.stream.Collectors;

public record ContractSummary(List<ContractClauseCode> clauses) {
    public ContractSummary {
        clauses = List.copyOf(clauses);
    }

    public String render() {
        return clauses.stream().map(Enum::name).collect(Collectors.joining(","));
    }
}
