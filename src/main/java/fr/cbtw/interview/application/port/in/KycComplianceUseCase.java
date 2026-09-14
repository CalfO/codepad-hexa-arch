package fr.cbtw.interview.application.port.in;

import java.time.LocalDate;

public interface KycComplianceUseCase {
    String checkCompliance(String documentType, LocalDate issueDate, LocalDate expiryDate,
                            String fullName, String address, double declaredIncome,
                            boolean isPoliticallyExposed);
}
