package fr.cbtw.interview.domain.payment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PaymentOrder {
    private static final double URGENT_LIMIT = 100_000;

    private final AccountIdentifier debtor;
    private final AccountIdentifier creditor;
    private final PaymentAmount amount;
    private final PaymentCurrency currency;
    private final LocalDate requestedExecutionDate;
    private final boolean urgent;

    private PaymentOrder(AccountIdentifier debtor, AccountIdentifier creditor, PaymentAmount amount,
                         PaymentCurrency currency, LocalDate requestedExecutionDate, boolean urgent) {
        this.debtor = debtor;
        this.creditor = creditor;
        this.amount = amount;
        this.currency = currency;
        this.requestedExecutionDate = requestedExecutionDate;
        this.urgent = urgent;
    }

    // Statement order mirrors the legacy service: accounts, amount, currency, urgency rules.
    // The execution date needs "today", so it is checked in scheduleAt().
    public static PaymentOrder submit(String debtorAccount, String creditorAccount, double amount, String currency,
                                      LocalDate requestedExecutionDate, boolean urgent) {
        AccountIdentifier debtor = new AccountIdentifier(debtorAccount);
        AccountIdentifier creditor = new AccountIdentifier(creditorAccount);
        if (debtor.equals(creditor)) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.IDENTICAL_ACCOUNTS);
        }
        PaymentAmount paymentAmount = new PaymentAmount(amount);
        PaymentCurrency paymentCurrency = PaymentCurrency.fromCode(currency);
        if (urgent && paymentCurrency != PaymentCurrency.EUR) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.URGENT_ONLY_IN_EUR);
        }
        if (urgent && paymentAmount.exceeds(URGENT_LIMIT)) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.URGENT_AMOUNT_ABOVE_LIMIT);
        }
        return new PaymentOrder(debtor, creditor, paymentAmount, paymentCurrency, requestedExecutionDate, urgent);
    }

    /**
     * Instant payments execute on the requested date, weekends included. Standard ones roll to the next
     * day once the currency cut-off has passed (strictly after: an order at exactly 16:00:00 still makes it),
     * then skip the weekend. A missing date is reported as "in the past", like the legacy.
     */
    public ScheduledPaymentOrder scheduleAt(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (requestedExecutionDate == null || requestedExecutionDate.isBefore(today)) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.EXECUTION_DATE_IN_THE_PAST);
        }
        if (urgent) {
            return new ScheduledPaymentOrder(this, requestedExecutionDate, ExecutionChannel.INSTANT);
        }
        LocalDate executionDate = requestedExecutionDate;
        if (executionDate.equals(today) && now.toLocalTime().isAfter(currency.cutOff())) {
            executionDate = executionDate.plusDays(1);
        }
        while (isWeekend(executionDate)) {
            executionDate = executionDate.plusDays(1);
        }
        return new ScheduledPaymentOrder(this, executionDate, ExecutionChannel.STANDARD);
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
