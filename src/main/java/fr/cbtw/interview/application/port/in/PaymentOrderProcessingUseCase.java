package fr.cbtw.interview.application.port.in;

import java.time.LocalDate;

public interface PaymentOrderProcessingUseCase {
    String submit(String reference, String debtorIban, String creditorIban, String creditorBic, double amount,
                  String currency, LocalDate requestedExecutionDate, boolean urgent);
}
