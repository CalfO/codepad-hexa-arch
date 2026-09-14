package fr.cbtw.interview.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

public final class ImplementationLoader {
     private static final String DEFAULT_SEARCH_PACKAGE = "fr.cbtw.interview.domain";

    private ImplementationLoader() {
    }

    /**
     * Cherche une implémentation du port sous le package par défaut fr.cbtw.interview.domain
     * (recherche récursive : peu importe le sous-package exact choisi par le candidat, par
     * exemple domain.service, domain.contract.service ou domain.service.contract).
     */
    public static <T> T findImplementationOf(Class<T> portInterface) {
        return findImplementationOf(portInterface, DEFAULT_SEARCH_PACKAGE);
    }

    /** Cherche une implémentation du port dans un package donné et ses sous-packages. */
    public static <T> T findImplementationOf(Class<T> portInterface, String searchPackage) {
        List<Class<?>> candidates = ClasspathScanner.findClassesInPackage(searchPackage);
        for (Class<?> candidate : candidates) {
            boolean isUsableImplementation = portInterface.isAssignableFrom(candidate)
                && !candidate.isInterface()
                && !Modifier.isAbstract(candidate.getModifiers());
            if (isUsableImplementation) {
                return instantiate(candidate, portInterface);
            }
        }
        throw new AssertionError(
            "Aucune classe sous le package '" + searchPackage + "' (ni ses sous-packages) n'implémente "
            + portInterface.getSimpleName()
            + ". Le candidat n'a pas (encore) fourni d'implémentation du use case à cet emplacement.");
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiate(Class<?> candidate, Class<T> portInterface) {
        try {
            Constructor<?> constructor = candidate.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "Impossible d'instancier " + candidate.getName()
                + " — vérifiez qu'un constructeur public (ou package-private) sans argument existe.", e);
        }
    }
}
