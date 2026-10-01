package fr.cbtw.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.cbtw.interview.application.port.in.KycComplianceUseCase;
import fr.cbtw.interview.legacy.KycComplianceService;
import fr.cbtw.interview.utils.ImplementationLoader;

/**
 * Double run en miniature : même entrée, même sortie que le legacy, au caractère près.
 * Les dates sont exprimées en jours relatifs à aujourd'hui (NULL = date absente).
 */
@DisplayName("Conformité KYC — parité avec le legacy")
class KycComplianceParityTest {
    private final KycComplianceUseCase legacy = new KycComplianceService();

    @ParameterizedTest(name = "[{index}] {0} émis J{1} expire J{2} nom={3} revenu={5} PPE={6}")
    @CsvSource(nullValues = "NULL", value = {
        // type, émission (jours), expiration (jours), nom, adresse, revenu déclaré, PPE
        "ID_CARD,          -730,  1095, Jean Dupont,  12 rue de la Paix,  2500, false",
        "PASSPORT,         -365,  1825, Marie Curie,  1 rue du Radium,    5000, true",
        "RESIDENCE_PERMIT, -100,  200,  Ana Lopez,    3 quai Est,         1800, false",
        "PASSPORT,         -30,   3000, Li Wei,       8 bd Ouest,         500,  false",
        "ID_CARD,          -400,  400,  Omar Diallo,  2 place Nord,       250,  true",
        "PASSPORT,         -1000, 50,   Eva Novak,    9 allée Sud,        12000,true",
        "ID_CARD,          -3650, -1,   Paul Martin,  5 avenue Foch,      3000, false",
        "PASSPORT,         -3650, -400, Paul Martin,  5 avenue Foch,      3000, true",
        "ID_CARD,          -10,   NULL, Paul Martin,  5 avenue Foch,      3000, false",
        "ID_CARD,          10,    400,  Paul Martin,  5 avenue Foch,      3000, false",
        "PASSPORT,         365,   3000, Paul Martin,  5 avenue Foch,      3000, false",
        "NULL,             -730,  1095, Jean Dupont,  12 rue de la Paix,  2500, false",
        "'',               -730,  1095, Jean Dupont,  12 rue de la Paix,  2500, false",
        "DRIVING_LICENCE,  -730,  1095, Jean Dupont,  12 rue de la Paix,  2500, false",
        "VISA,             -730,  -5,   NULL,         NULL,               2500, false",
        "ID_CARD,          -730,  1095, NULL,         12 rue de la Paix,  2500, false",
        "ID_CARD,          -730,  1095, Jean Dupont,  NULL,               2500, false",
        "ID_CARD,          -730,  1095, '',           12 rue de la Paix,  2500, false",
        "PASSPORT,         -730,  -30,  NULL,         12 rue de la Paix,  2500, false",
        "RESIDENCE_PERMIT, -50,   50,   Sam Lee,      4 rue Haute,        1500, true",
        "ID_CARD,          -200,  800,  Nora Klein,   7 rue Basse,        4200, false",
        "PASSPORT,         -20,   5000, Igor Petrov,  6 rue Neuve,        300,  false",
    })
    void producesExactlyTheSameResultAsLegacy(String documentType, Integer issueInDays, Integer expiryInDays,
                                               String fullName, String address, double declaredIncome,
                                               boolean politicallyExposed) {
        LocalDate issueDate = relativeToToday(issueInDays);
        LocalDate expiryDate = relativeToToday(expiryInDays);
        KycComplianceUseCase candidate = ImplementationLoader.findImplementationOf(KycComplianceUseCase.class);
        assertEquals(
            legacy.checkCompliance(documentType, issueDate, expiryDate, fullName, address, declaredIncome, politicallyExposed),
            candidate.checkCompliance(documentType, issueDate, expiryDate, fullName, address, declaredIncome, politicallyExposed));
    }

    private static LocalDate relativeToToday(Integer days) {
        return days == null ? null : LocalDate.now().plusDays(days);
    }
}
