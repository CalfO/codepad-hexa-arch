package fr.cbtw.interview.application.port.out;

/**
 * Port de sortie : enregistre une réservation de fonds (paiement autorisé ou soumis à approbation).
 *
 * @param <R> le type métier qui représente une réservation enregistrée — à vous de le définir.
 */
public interface FundsReservationRepository<R> {
    void save(R reservation);
}
