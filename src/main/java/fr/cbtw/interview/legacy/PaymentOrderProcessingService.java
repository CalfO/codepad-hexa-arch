package fr.cbtw.interview.legacy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.cbtw.interview.application.port.in.PaymentOrderProcessingUseCase;

public class PaymentOrderProcessingService implements PaymentOrderProcessingUseCase {
    // reference, debtorIban, creditorIban, amountInEur, executionDate, status
    static List<String[]> savedOrders = new ArrayList<>();
    static final Map<String, Double> EUR_RATES = Map.of("EUR", 1.0, "USD", 0.92, "GBP", 1.17, "CHF", 1.04);
    static final List<String> SEPA_COUNTRIES = List.of("FR", "DE", "ES", "IT", "BE", "NL", "LU", "PT", "IE", "AT");

    public String submit(String reference, String debtorIban, String creditorIban, String creditorBic, double amount,
                         String currency, LocalDate requestedExecutionDate, boolean urgent) {
        if (reference == null || reference.isBlank()) {
            return "ERROR: missing reference";
        }
        for (String[] saved : savedOrders) {
            if (saved[0].equals(reference)) {
                return "DUPLICATE: " + reference;
            }
        }
        if (debtorIban == null || !debtorIban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}")) {
            return "ERROR: invalid debtor IBAN";
        }
        if (creditorIban == null || !creditorIban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}")) {
            return "ERROR: invalid creditor IBAN";
        }
        if (debtorIban.equals(creditorIban)) {
            return "REJECTED: debtor and creditor accounts are identical";
        }
        if (amount <= 0 || Math.round(amount * 100) != amount * 100) {
            return "ERROR: invalid amount";
        }
        if (currency == null || !EUR_RATES.containsKey(currency)) {
            return "ERROR: unsupported currency";
        }

        boolean sepaReachable = currency.equals("EUR")
            && SEPA_COUNTRIES.contains(debtorIban.substring(0, 2))
            && SEPA_COUNTRIES.contains(creditorIban.substring(0, 2));
        String scheme;
        if (sepaReachable && urgent && amount <= 100000) {
            scheme = "SEPA_INSTANT";
        } else if (sepaReachable && urgent) {
            scheme = "TARGET2";
        } else if (sepaReachable) {
            scheme = "SEPA";
        } else {
            scheme = "SWIFT";
        }
        if (scheme.equals("SWIFT") && (creditorBic == null || (creditorBic.length() != 8 && creditorBic.length() != 11))) {
            return "REJECTED: BIC required for SWIFT payment";
        }

        LocalDate today = LocalDate.now();
        if (requestedExecutionDate == null) {
            requestedExecutionDate = today;
        }
        if (requestedExecutionDate.isBefore(today)) {
            return "REJECTED: execution date in the past";
        }
        LocalDate executionDate = requestedExecutionDate;
        if (!scheme.equals("SEPA_INSTANT")) {
            LocalTime cutOff;
            if (scheme.equals("SEPA")) {
                cutOff = LocalTime.of(16, 0);
            } else if (scheme.equals("TARGET2")) {
                cutOff = LocalTime.of(17, 0);
            } else {
                cutOff = currency.equals("USD") ? LocalTime.of(15, 0) : LocalTime.of(13, 0);
            }
            if (executionDate.equals(today) && !LocalTime.now().isBefore(cutOff)) {
                executionDate = executionDate.plusDays(1);
            }
            while (executionDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || executionDate.getDayOfWeek() == DayOfWeek.SUNDAY
                || (executionDate.getMonthValue() == 12 && executionDate.getDayOfMonth() == 25)
                || (executionDate.getMonthValue() == 1 && executionDate.getDayOfMonth() == 1)) {
                executionDate = executionDate.plusDays(1);
            }
        }

        double fees;
        if (scheme.equals("SEPA")) {
            fees = 0;
        } else if (scheme.equals("SEPA_INSTANT")) {
            fees = 0.5;
        } else if (scheme.equals("TARGET2")) {
            fees = 15;
        } else {
            fees = Math.min(25 + amount * EUR_RATES.get(currency) * 0.001, 100);
        }
        if (urgent && scheme.equals("SWIFT")) {
            fees = fees + 30;
        }

        double amountInEur = amount * EUR_RATES.get(currency);
        double alreadyCommitted = 0;
        for (String[] saved : savedOrders) {
            if (saved[1].equals(debtorIban) && saved[4].equals(executionDate.toString()) && !saved[5].equals("REJECTED")) {
                alreadyCommitted += Double.parseDouble(saved[3]);
            }
        }
        String status = alreadyCommitted + amountInEur > 1000000 ? "PENDING_APPROVAL" : "ACCEPTED";

        savedOrders.add(new String[] {reference, debtorIban, creditorIban, String.valueOf(amountInEur),
            executionDate.toString(), status});
        if (status.equals("PENDING_APPROVAL")) {
            System.out.println("[TREASURY] approval required for " + reference + " (" + debtorIban + ", "
                + (alreadyCommitted + amountInEur) + " EUR committed on " + executionDate + ")");
        }
        return status + ";scheme=" + scheme + ";execution=" + executionDate
            + ";fees=" + String.format(Locale.ROOT, "%.2f", fees);
    }
}
