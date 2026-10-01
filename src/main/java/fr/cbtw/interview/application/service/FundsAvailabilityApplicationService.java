package fr.cbtw.interview.application.service;

import fr.cbtw.interview.application.port.in.FundsAvailabilityUseCase;
import fr.cbtw.interview.application.port.out.FundsReservationRepository;
import fr.cbtw.interview.domain.funds.FundsCheck;
import fr.cbtw.interview.domain.funds.FundsCheckRejectedException;
import fr.cbtw.interview.domain.funds.FundsReservation;

public final class FundsAvailabilityApplicationService implements FundsAvailabilityUseCase {
    private final FundsReservationRepository<FundsReservation> repository;

    public FundsAvailabilityApplicationService(FundsReservationRepository<FundsReservation> repository) {
        this.repository = repository;
    }

    @Override
    public String checkFunds(String accountStatus, double ledgerBalance, double pendingDebits,
                             double authorizedOverdraft, double paymentAmount) {
        try {
            FundsReservation reservation = FundsCheck.reserve(accountStatus, ledgerBalance, pendingDebits,
                authorizedOverdraft, paymentAmount);
            repository.save(reservation);
            return reservation.render();
        } catch (FundsCheckRejectedException e) {
            return switch (e.reason()) {
                case UNKNOWN_ACCOUNT_STATUS -> "ERROR: unknown account status";
                case ACCOUNT_CLOSED -> "REJECTED: account closed";
                case ACCOUNT_BLOCKED -> "REJECTED: account blocked";
                case INVALID_PAYMENT_AMOUNT -> "ERROR: invalid payment amount";
                case INVALID_OVERDRAFT -> "ERROR: invalid overdraft";
                case INSUFFICIENT_FUNDS -> "REJECTED: insufficient funds";
            };
        }
    }
}
