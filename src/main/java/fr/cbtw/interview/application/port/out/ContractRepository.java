package fr.cbtw.interview.application.port.out;

/**
 * Port de sortie : enregistre la synthèse des clauses d'un contrat.
 *
 * @param <C> le type métier qui représente une synthèse enregistrée — à vous de le définir.
 */
public interface ContractRepository<C> {
    void save(C contract);
}
