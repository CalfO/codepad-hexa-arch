package fr.cbtw.interview.application.port.out;

/**
 * Port de sortie : enregistre un ordre de paiement accepté.
 *
 * @param <O> le type métier qui représente un ordre enregistré — à vous de le définir.
 */
public interface PaymentOrderRepository<O> {
    void save(O order);
}
