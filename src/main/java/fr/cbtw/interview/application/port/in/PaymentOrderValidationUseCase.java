package fr.cbtw.interview.application.port.in;

import java.time.LocalDate;

public interface PaymentOrderValidationUseCase {
    String validate(String debtorAccount, String creditorAccount, double amount, String currency,
                    LocalDate requestedExecutionDate, boolean urgent);
}
