package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.cbtw.interview.utils.ClasspathScanner;

public class HexagonalArchitectureTest {
    
    @Test
    void domainMustNotDependOnInfrastructure() {
        List<Class<?>> domainClasses = ClasspathScanner.findClassesInPackage("domain");
        for (Class<?> domainClass : domainClasses) {
            for (Field field : domainClass.getDeclaredFields()) {
                assertFalse(field.getType().getPackageName().startsWith("infrastructure"),
                    domainClass.getSimpleName() + " ne doit pas référencer infrastructure.* (champ " + field.getName() + ")");
            }
            for (Method method : domainClass.getDeclaredMethods()) {
                for (Parameter p : method.getParameters()) {
                    assertFalse(p.getType().getPackageName().startsWith("infrastructure"),
                        domainClass.getSimpleName() + " ne doit pas dépendre de infrastructure.* dans sa signature");
                }
            }
        }
    }

    @Test
    void atLeastOneValueObjectExistsInDomainModel() {
        List<Class<?>> modelClasses = ClasspathScanner.findClassesInPackage("domain.model");
        assertFalse(modelClasses.isEmpty(), "Aucune classe trouvée dans domain.model — aucun Value Object/Entité extrait");
    }

    @Test
    void valueObjectCandidatesShouldBeImmutable() {
        // Heuristique, pas une preuve formelle : on vérifie que les champs sont 'final'
        // sur les classes de domain.model qui ne sont pas des enums.
        List<Class<?>> modelClasses = ClasspathScanner.findClassesInPackage("domain.model");
        for (Class<?> c : modelClasses) {
            if (c.isEnum()) continue;
            for (Field f : c.getDeclaredFields()) {
                assertTrue(Modifier.isFinal(f.getModifiers()),
                    c.getSimpleName() + "." + f.getName() + " n'est pas final — immuabilité non garantie");
            }
        }
    }
}
