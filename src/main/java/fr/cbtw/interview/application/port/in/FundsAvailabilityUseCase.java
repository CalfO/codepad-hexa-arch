package fr.cbtw.interview.application.port.in;

public interface FundsAvailabilityUseCase {
    String checkFunds(String accountStatus, double ledgerBalance, double pendingDebits,
                      double authorizedOverdraft, double paymentAmount);
}
