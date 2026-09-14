package fr.cbtw.interview.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

public final class ImplementationLoader {
     private static final String DEFAULT_SEARCH_PACKAGE = "domain.service";

    private ImplementationLoader() {
    }

    /** Cherche une implémentation du port dans le package par défaut domain.service. */
    public static <T> T findImplementationOf(Class<T> portInterface) {
        return findImplementationOf(portInterface, DEFAULT_SEARCH_PACKAGE);
    }

    /** Cherche une implémentation du port dans un package donné. */
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
            "Aucune classe dans le package '" + searchPackage + "' n'implémente "
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
