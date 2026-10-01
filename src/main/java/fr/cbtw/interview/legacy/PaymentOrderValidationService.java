package fr.cbtw.interview.legacy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.in.PaymentOrderValidationUseCase;

public class PaymentOrderValidationService implements PaymentOrderValidationUseCase {
    static List<String> savedOrders = new ArrayList<>();

    public String validate(String debtorAccount, String creditorAccount, double amount, String currency,
                           LocalDate requestedExecutionDate, boolean urgent) {
        if (debtorAccount == null || debtorAccount.isBlank() || creditorAccount == null || creditorAccount.isBlank()) {
            return "ERROR: missing account";
        }
        if (debtorAccount.equals(creditorAccount)) {
            return "REJECTED: debtor and creditor accounts are identical";
        }
        if (amount <= 0) {
            return "ERROR: invalid amount";
        }
        if (Math.round(amount * 100) != amount * 100) {
            return "ERROR: amount has more than 2 decimals";
        }
        if (currency == null || (!currency.equals("EUR") && !currency.equals("USD")
            && !currency.equals("GBP") && !currency.equals("CHF"))) {
            return "ERROR: unsupported currency";
        }
        if (urgent && !currency.equals("EUR")) {
            return "REJECTED: urgent payment only available in EUR";
        }
        if (urgent && amount > 100000) {
            return "REJECTED: urgent payment above 100000 EUR";
        }
        LocalDate today = LocalDate.now();
        if (requestedExecutionDate == null || requestedExecutionDate.isBefore(today)) {
            return "REJECTED: execution date in the past";
        }
        LocalDate executionDate = requestedExecutionDate;
        if (!urgent) {
            LocalTime cutOff = currency.equals("EUR") ? LocalTime.of(16, 0) : LocalTime.of(14, 0);
            if (executionDate.equals(today) && LocalTime.now().isAfter(cutOff)) {
                executionDate = executionDate.plusDays(1);
            }
            while (executionDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || executionDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                executionDate = executionDate.plusDays(1);
            }
        }
        String channel = urgent ? "INSTANT" : "STANDARD";
        savedOrders.add(debtorAccount + ">" + creditorAccount + ";" + amount + " " + currency + ";"
            + executionDate + ";" + channel);
        return "ACCEPTED: execution=" + executionDate + ";channel=" + channel;
    }
}
