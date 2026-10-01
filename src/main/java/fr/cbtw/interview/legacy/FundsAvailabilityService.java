package fr.cbtw.interview.legacy;

import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.in.FundsAvailabilityUseCase;

public class FundsAvailabilityService implements FundsAvailabilityUseCase {
    static List<String> savedReservations = new ArrayList<>();

    public String checkFunds(String accountStatus, double ledgerBalance, double pendingDebits,
                             double authorizedOverdraft, double paymentAmount) {
        if (accountStatus == null) {
            return "ERROR: unknown account status";
        }
        if (accountStatus.equals("CLOSED")) {
            return "REJECTED: account closed";
        }
        if (accountStatus.equals("BLOCKED")) {
            return "REJECTED: account blocked";
        }
        if (!accountStatus.equals("ACTIVE")) {
            return "ERROR: unknown account status";
        }
        if (paymentAmount <= 0) {
            return "ERROR: invalid payment amount";
        }
        if (authorizedOverdraft < 0) {
            return "ERROR: invalid overdraft";
        }
        double available = ledgerBalance - pendingDebits + authorizedOverdraft;
        if (paymentAmount > available) {
            return "REJECTED: insufficient funds";
        }
        double balanceAfterPayment = ledgerBalance - pendingDebits - paymentAmount;
        String decision;
        if (balanceAfterPayment >= 0) {
            decision = "AUTHORIZED";
        } else {
            double overdraftUsage = -balanceAfterPayment / authorizedOverdraft;
            decision = overdraftUsage > 0.8 ? "PENDING_APPROVAL" : "AUTHORIZED_WITH_OVERDRAFT";
        }
        double remaining = available - paymentAmount;
        savedReservations.add(decision + ":" + paymentAmount);
        return decision + ": remaining=" + remaining;
    }
}
