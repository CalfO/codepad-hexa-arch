package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import fr.cbtw.interview.utils.ClasspathScanner;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Respect de l'architecture hexagonale")
public class HexagonalArchitectureTest {
    private static final String DOMAIN = "fr.cbtw.interview.domain";
    private static final String APPLICATION = "fr.cbtw.interview.application";

    @Test
    @DisplayName("Le cœur (domain, application) ne référence aucune classe d'infrastructure")
    void coreMustNotDependOnInfrastructure() {
        // Lecture du bytecode : détecte aussi un `new InMemory...()` dans un constructeur ou un import
        // utilisé seulement dans le corps d'une méthode, pas seulement les champs et signatures.
        List<String> offenders = coreClasses()
            .filter(c -> referencesPackage(c, "fr/cbtw/interview/infrastructure/"))
            .map(Class::getName).toList();
        assertTrue(offenders.isEmpty(),
            "Ces classes du cœur référencent infrastructure.* (le câblage doit venir de l'extérieur) : " + offenders);
    }

    @Test
    @DisplayName("Le cœur (domain, application) ne délègue pas au code legacy")
    void coreMustNotDelegateToLegacy() {
        List<String> offenders = coreClasses()
            .filter(c -> referencesPackage(c, "fr/cbtw/interview/legacy/"))
            .map(Class::getName).toList();
        assertTrue(offenders.isEmpty(), "Ces classes du cœur appellent encore le legacy : " + offenders);
    }

    @Test
    @DisplayName("Le domaine contient au moins un type métier")
    void domainContainsAtLeastOneBusinessType() {
        assertFalse(ClasspathScanner.findClassesInPackage(DOMAIN).isEmpty(),
            "Aucune classe trouvée sous " + DOMAIN + " (ni ses sous-packages) — aucun concept métier extrait");
    }

    @Test
    @DisplayName("Les types du domaine sont immuables (champs final)")
    void domainTypesShouldBeImmutable() {
        // Heuristique, pas une preuve formelle : tout champ déclaré (statique compris) doit être final.
        for (Class<?> c : ClasspathScanner.findClassesInPackage(DOMAIN)) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isSynthetic()) {
                    continue;
                }
                assertTrue(Modifier.isFinal(f.getModifiers()),
                    c.getSimpleName() + "." + f.getName() + " n'est pas final — immuabilité non garantie");
            }
        }
    }

    private static Stream<Class<?>> coreClasses() {
        return Stream.of(DOMAIN, APPLICATION).flatMap(p -> ClasspathScanner.findClassesInPackage(p).stream());
    }

    private static boolean referencesPackage(Class<?> c, String internalPackageName) {
        String resource = c.getName().replace('.', '/') + ".class";
        try (InputStream in = c.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Bytecode introuvable pour " + c.getName());
            }
            // Les noms de classes référencées figurent en clair (UTF-8 modifié) dans le constant pool.
            return new String(in.readAllBytes(), StandardCharsets.ISO_8859_1).contains(internalPackageName);
        } catch (IOException e) {
            throw new IllegalStateException("Lecture du bytecode impossible pour " + c.getName(), e);
        }
    }
}
