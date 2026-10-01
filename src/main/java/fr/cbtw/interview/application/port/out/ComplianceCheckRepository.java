package fr.cbtw.interview.application.port.out;

/**
 * Port de sortie : enregistre le résultat d'un contrôle KYC abouti.
 *
 * @param <C> le type métier qui représente un contrôle enregistré — à vous de le définir.
 */
public interface ComplianceCheckRepository<C> {
    void save(C check);
}
