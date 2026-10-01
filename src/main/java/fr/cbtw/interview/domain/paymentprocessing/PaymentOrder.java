package fr.cbtw.interview.domain.paymentprocessing;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PaymentOrder {
    private final PaymentReference reference;
    private final Iban debtor;
    private final Iban creditor;
    private final Money money;
    private final PaymentScheme scheme;
    private final LocalDate requestedExecutionDate;
    private final boolean urgent;

    private PaymentOrder(PaymentReference reference, Iban debtor, Iban creditor, Money money, PaymentScheme scheme,
                         LocalDate requestedExecutionDate, boolean urgent) {
        this.reference = reference;
        this.debtor = debtor;
        this.creditor = creditor;
        this.money = money;
        this.scheme = scheme;
        this.requestedExecutionDate = requestedExecutionDate;
        this.urgent = urgent;
    }

    // Statement order mirrors the legacy service (reference and duplicate check happen before, in the
    // application service, because the duplicate check needs the journal).
    public static PaymentOrder submit(PaymentReference reference, String debtorIban, String creditorIban,
                                      String creditorBic, double amount, String currencyCode,
                                      LocalDate requestedExecutionDate, boolean urgent) {
        if (!Iban.isWellFormed(debtorIban)) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.INVALID_DEBTOR_IBAN);
        }
        if (!Iban.isWellFormed(creditorIban)) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.INVALID_CREDITOR_IBAN);
        }
        Iban debtor = new Iban(debtorIban);
        Iban creditor = new Iban(creditorIban);
        if (debtor.equals(creditor)) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.IDENTICAL_ACCOUNTS);
        }
        Money.checkAmount(amount);
        Currency currency = Currency.fromCode(currencyCode)
            .orElseThrow(() -> new PaymentOrderRejectedException(PaymentOrderRejection.UNSUPPORTED_CURRENCY));
        Money money = new Money(amount, currency);
        PaymentScheme scheme = PaymentScheme.select(SepaZone.reaches(debtor, creditor, currency), urgent, money);
        if (scheme == PaymentScheme.SWIFT && !Bic.isUsable(creditorBic)) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.BIC_REQUIRED);
        }
        return new PaymentOrder(reference, debtor, creditor, money, scheme, requestedExecutionDate, urgent);
    }

    /** A missing requested date means "as soon as possible", i.e. today. */
    public ScheduledPayment scheduleAt(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate requested = requestedExecutionDate == null ? today : requestedExecutionDate;
        if (requested.isBefore(today)) {
            throw new PaymentOrderRejectedException(PaymentOrderRejection.EXECUTION_DATE_IN_THE_PAST);
        }
        LocalDate executionDate = scheme.cutOff(money.currency())
            .map(cutOff -> {
                boolean missedCutOff = requested.equals(today) && !now.toLocalTime().isBefore(cutOff);
                return BankingCalendar.firstBusinessDayFrom(missedCutOff ? requested.plusDays(1) : requested);
            })
            .orElse(requested);
        return new ScheduledPayment(this, scheme, executionDate, scheme.fees(money, urgent));
    }

    public PaymentReference reference() {
        return reference;
    }

    public Iban debtor() {
        return debtor;
    }

    public Iban creditor() {
        return creditor;
    }

    public Money money() {
        return money;
    }
}
