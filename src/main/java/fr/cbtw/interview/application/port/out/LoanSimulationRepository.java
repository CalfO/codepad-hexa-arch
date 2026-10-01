package fr.cbtw.interview.application.port.out;

/**
 * Port de sortie : enregistre une simulation de prêt acceptée.
 *
 * @param <S> le type métier qui représente une simulation enregistrée — à vous de le définir.
 */
public interface LoanSimulationRepository<S> {
    void save(S simulation);
}
