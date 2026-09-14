package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import fr.cbtw.interview.utils.ClasspathScanner;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Respect de l'architecture hexagonale")
public class HexagonalArchitectureTest {

    @Test
    @DisplayName("Le domaine ne doit dépendre d'aucune classe d'infrastructure")
    void domainMustNotDependOnInfrastructure() {
        // Scan récursif : couvre domain.model, domain.service, et tout sous-package
        // additionnel que le candidat choisirait (domain.contract, domain.service.kyc, etc.).
        List<Class<?>> domainClasses = ClasspathScanner.findClassesInPackage("fr.cbtw.interview.domain");
        for (Class<?> domainClass : domainClasses) {
            for (Field field : domainClass.getDeclaredFields()) {
                assertFalse(field.getType().getPackageName().startsWith("fr.cbtw.interview.infrastructure"),
                    domainClass.getSimpleName() + " ne doit pas référencer infrastructure.* (champ " + field.getName() + ")");
            }
            for (Method method : domainClass.getDeclaredMethods()) {
                for (Parameter p : method.getParameters()) {
                    assertFalse(p.getType().getPackageName().startsWith("fr.cbtw.interview.infrastructure"),
                        domainClass.getSimpleName() + " ne doit pas dépendre de infrastructure.* dans sa signature");
                }
            }
        }
    }

    @Test
    @DisplayName("Au moins un Value Object/Entité existe dans domain.model")
    void atLeastOneValueObjectExistsInDomainModel() {
        List<Class<?>> modelClasses = ClasspathScanner.findClassesInPackage("fr.cbtw.interview.domain.model");
        assertFalse(modelClasses.isEmpty(), "Aucune classe trouvée dans domain.model — aucun Value Object/Entité extrait");
    }

    @Test
    @DisplayName("Les classes de domain.model doivent être immuables (champs final)")
    void valueObjectCandidatesShouldBeImmutable() {
        // Heuristique, pas une preuve formelle : on vérifie que les champs sont 'final'
        // sur les classes de domain.model qui ne sont pas des enums.
        List<Class<?>> modelClasses = ClasspathScanner.findClassesInPackage("fr.cbtw.interview.domain.model");
        for (Class<?> c : modelClasses) {
            if (c.isEnum()) continue;
            for (Field f : c.getDeclaredFields()) {
                assertTrue(Modifier.isFinal(f.getModifiers()),
                    c.getSimpleName() + "." + f.getName() + " n'est pas final — immuabilité non garantie");
            }
        }
    }
}
