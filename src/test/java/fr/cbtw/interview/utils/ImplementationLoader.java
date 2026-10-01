package fr.cbtw.interview.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Retrouve l'implémentation d'un use case et la câble : le test joue le rôle de composition root.
 *
 * <ul>
 *   <li>L'implémentation du port d'entrée est cherchée (récursivement) sous {@code fr.cbtw.interview.domain}
 *       et {@code fr.cbtw.interview.application}.</li>
 *   <li>Elle est construite via son constructeur public le plus riche dont tous les paramètres sont injectables :
 *       <ul>
 *         <li>{@link Clock} : horloge système par défaut, ou celle fournie via {@link Wiring#provide};</li>
 *         <li>interface ou classe abstraite (typiquement un port de sortie) : l'unique adapter concret trouvé sous
 *             {@code fr.cbtw.interview.infrastructure}, lui-même construit selon les mêmes règles;</li>
 *         <li>classe concrète de {@code fr.cbtw.interview.*} : construite selon les mêmes règles.</li>
 *       </ul>
 *   </li>
 * </ul>
 * Chaque {@link Wiring} crée ses propres instances (une par classe concrète) : deux appels à
 * {@link #findImplementationOf} ne partagent aucun état.
 */
public final class ImplementationLoader {
    private static final List<String> USE_CASE_PACKAGES =
        List.of("fr.cbtw.interview.domain", "fr.cbtw.interview.application");
    private static final String ADAPTER_PACKAGE = "fr.cbtw.interview.infrastructure";
    private static final String PROJECT_PACKAGE = "fr.cbtw.interview.";
    private static final int MAX_DEPTH = 6;

    private ImplementationLoader() {
    }

    public static <T> T findImplementationOf(Class<T> portInterface) {
        return wiring().implementationOf(portInterface);
    }

    public static Wiring wiring() {
        return new Wiring();
    }

    public static final class Wiring {
        private final Map<Class<?>, Object> provided = new HashMap<>();
        private final Map<Class<?>, Object> instances = new LinkedHashMap<>();
        private final Set<Class<?>> injectedProvidedTypes = new HashSet<>();

        private Wiring() {
            provided.put(Clock.class, Clock.systemDefaultZone());
        }

        /** Remplace l'instance injectée pour ce type (ex. une {@link Clock} fixe). */
        public <S> Wiring provide(Class<S> type, S instance) {
            provided.put(type, instance);
            return this;
        }

        /** Vrai si une dépendance de ce type (fournie par le harness) a été injectée quelque part. */
        public boolean hasInjected(Class<?> type) {
            return injectedProvidedTypes.contains(type);
        }

        /** Instance créée par ce câblage pour un type donné (ex. l'adapter en mémoire), si elle existe. */
        public <S> Optional<S> instanceOf(Class<S> type) {
            return instances.values().stream().filter(type::isInstance).map(type::cast).findFirst();
        }

        public <T> T implementationOf(Class<T> portInterface) {
            List<Class<?>> implementations = concreteSubtypesOf(portInterface, USE_CASE_PACKAGES);
            if (implementations.isEmpty()) {
                throw new AssertionError(
                    "Aucune classe sous " + USE_CASE_PACKAGES + " (ni leurs sous-packages) n'implémente "
                    + portInterface.getSimpleName()
                    + ". L'implémentation du use case n'a pas (encore) été fournie à cet emplacement.");
            }
            if (implementations.size() > 1) {
                throw new AssertionError("Plusieurs implémentations de " + portInterface.getSimpleName()
                    + " trouvées, le harness ne sait pas laquelle tester : " + names(implementations));
            }
            try {
                return portInterface.cast(build(implementations.get(0), new ArrayDeque<>()));
            } catch (Unresolvable e) {
                throw new AssertionError(e.getMessage(), e);
            }
        }

        private Object resolve(Class<?> type, Deque<Class<?>> path) {
            if (provided.containsKey(type)) {
                injectedProvidedTypes.add(type);
                return provided.get(type);
            }
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                return build(adapterFor(type), path);
            }
            if (type.getName().startsWith(PROJECT_PACKAGE) && !type.isEnum() && !type.isRecord()) {
                return build(type, path);
            }
            throw new Unresolvable("le harness ne sait pas fournir un " + type.getName());
        }

        private Class<?> adapterFor(Class<?> port) {
            List<Class<?>> adapters = concreteSubtypesOf(port, List.of(ADAPTER_PACKAGE));
            if (adapters.size() > 1) {
                List<Class<?>> inMemory = adapters.stream()
                    .filter(c -> c.getSimpleName().startsWith("InMemory")).toList();
                adapters = inMemory.size() == 1 ? inMemory : adapters;
            }
            if (adapters.isEmpty()) {
                throw new Unresolvable("aucun adapter sous " + ADAPTER_PACKAGE + " n'implémente " + port.getName());
            }
            if (adapters.size() > 1) {
                throw new Unresolvable("plusieurs adapters implémentent " + port.getName() + " : " + names(adapters));
            }
            return adapters.get(0);
        }

        private Object build(Class<?> concrete, Deque<Class<?>> path) {
            if (instances.containsKey(concrete)) {
                return instances.get(concrete);
            }
            if (path.contains(concrete) || path.size() > MAX_DEPTH) {
                throw new Unresolvable("dépendance circulaire ou trop profonde autour de " + concrete.getName());
            }
            path.push(concrete);
            try {
                List<Constructor<?>> constructors = Arrays.stream(concrete.getDeclaredConstructors())
                    .filter(c -> !Modifier.isPrivate(c.getModifiers()))
                    .sorted(Comparator.comparingInt(Constructor<?>::getParameterCount).reversed())
                    .toList();
                Unresolvable lastFailure = null;
                for (Constructor<?> constructor : constructors) {
                    Object[] arguments;
                    try {
                        arguments = Arrays.stream(constructor.getParameterTypes()).map(p -> resolve(p, path)).toArray();
                    } catch (Unresolvable e) {
                        lastFailure = e;
                        continue;
                    }
                    Object instance = instantiate(constructor, arguments);
                    instances.put(concrete, instance);
                    return instance;
                }
                throw new Unresolvable("impossible d'instancier " + concrete.getName()
                    + " : aucun constructeur non privé dont tous les paramètres sont injectables"
                    + (lastFailure == null ? "" : " (" + lastFailure.getMessage() + ")")
                    + ". Le harness sait injecter : java.time.Clock, un adapter de " + ADAPTER_PACKAGE
                    + " pour chaque port (interface), et toute classe concrète du projet construite selon les mêmes règles.");
            } finally {
                path.pop();
            }
        }

        private static Object instantiate(Constructor<?> constructor, Object[] arguments) {
            try {
                constructor.setAccessible(true);
                return constructor.newInstance(arguments);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Le constructeur de " + constructor.getDeclaringClass().getName()
                    + " a levé une exception", e.getCause());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Impossible d'appeler le constructeur de "
                    + constructor.getDeclaringClass().getName(), e);
            }
        }
    }

    private static List<Class<?>> concreteSubtypesOf(Class<?> type, List<String> packages) {
        return packages.stream()
            .flatMap(p -> ClasspathScanner.findClassesInPackage(p).stream())
            .filter(type::isAssignableFrom)
            .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
            .filter(c -> !c.isAnonymousClass() && !c.isSynthetic())
            .distinct()
            .sorted(Comparator.comparing(Class::getName))
            .toList();
    }

    private static String names(List<Class<?>> classes) {
        return classes.stream().map(Class::getName).collect(Collectors.joining(", "));
    }

    private static final class Unresolvable extends RuntimeException {
        Unresolvable(String message) {
            super(message);
        }
    }
}
