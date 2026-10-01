package fr.cbtw.interview.domain.paymentprocessing;

import java.time.LocalDate;
import java.util.Locale;

public record ProcessedPaymentOrder(PaymentReference reference, Iban debtor, Iban creditor, double amountInEur,
                                    PaymentScheme scheme, LocalDate executionDate, double fees,
                                    ProcessingStatus status) {
    public String render() {
        return status + ";scheme=" + scheme + ";execution=" + executionDate
            + ";fees=" + String.format(Locale.ROOT, "%.2f", fees);
    }
}
