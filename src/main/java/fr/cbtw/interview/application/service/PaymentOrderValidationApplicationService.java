package fr.cbtw.interview.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import fr.cbtw.interview.application.port.in.PaymentOrderValidationUseCase;
import fr.cbtw.interview.application.port.out.PaymentOrderRepository;
import fr.cbtw.interview.domain.payment.PaymentOrder;
import fr.cbtw.interview.domain.payment.PaymentOrderRejectedException;
import fr.cbtw.interview.domain.payment.ScheduledPaymentOrder;

public final class PaymentOrderValidationApplicationService implements PaymentOrderValidationUseCase {
    private final PaymentOrderRepository<ScheduledPaymentOrder> repository;
    private final Clock clock;

    public PaymentOrderValidationApplicationService(PaymentOrderRepository<ScheduledPaymentOrder> repository,
                                                    Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public String validate(String debtorAccount, String creditorAccount, double amount, String currency,
                           LocalDate requestedExecutionDate, boolean urgent) {
        try {
            PaymentOrder order = PaymentOrder.submit(debtorAccount, creditorAccount, amount, currency,
                requestedExecutionDate, urgent);
            // Read the time once: the legacy calls LocalDate.now() then LocalTime.now(), which can
            // straddle midnight. Same result in every other case.
            ScheduledPaymentOrder scheduled = order.scheduleAt(LocalDateTime.now(clock));
            repository.save(scheduled);
            return scheduled.render();
        } catch (PaymentOrderRejectedException e) {
            return switch (e.reason()) {
                case MISSING_ACCOUNT -> "ERROR: missing account";
                case IDENTICAL_ACCOUNTS -> "REJECTED: debtor and creditor accounts are identical";
                case INVALID_AMOUNT -> "ERROR: invalid amount";
                case TOO_MANY_DECIMALS -> "ERROR: amount has more than 2 decimals";
                case UNSUPPORTED_CURRENCY -> "ERROR: unsupported currency";
                case URGENT_ONLY_IN_EUR -> "REJECTED: urgent payment only available in EUR";
                case URGENT_AMOUNT_ABOVE_LIMIT -> "REJECTED: urgent payment above 100000 EUR";
                case EXECUTION_DATE_IN_THE_PAST -> "REJECTED: execution date in the past";
            };
        }
    }
}
