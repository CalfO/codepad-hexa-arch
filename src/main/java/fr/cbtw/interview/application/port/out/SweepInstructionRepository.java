package fr.cbtw.interview.application.port.out;

/**
 * Port de sortie : enregistre une instruction de sweeping émise.
 *
 * @param <I> le type métier qui représente une instruction enregistrée — à vous de le définir.
 */
public interface SweepInstructionRepository<I> {
    void save(I instruction);
}
