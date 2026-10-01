package fr.cbtw.interview.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import fr.cbtw.interview.application.port.in.PaymentOrderProcessingUseCase;
import fr.cbtw.interview.application.port.out.paymentprocessing.PaymentOrderJournal;
import fr.cbtw.interview.application.port.out.paymentprocessing.TreasuryNotifier;
import fr.cbtw.interview.domain.paymentprocessing.DailyLimit;
import fr.cbtw.interview.domain.paymentprocessing.PaymentOrder;
import fr.cbtw.interview.domain.paymentprocessing.PaymentOrderRejectedException;
import fr.cbtw.interview.domain.paymentprocessing.PaymentReference;
import fr.cbtw.interview.domain.paymentprocessing.ProcessedPaymentOrder;
import fr.cbtw.interview.domain.paymentprocessing.ProcessingStatus;
import fr.cbtw.interview.domain.paymentprocessing.ScheduledPayment;

public final class PaymentOrderProcessingApplicationService implements PaymentOrderProcessingUseCase {
    private final PaymentOrderJournal journal;
    private final TreasuryNotifier treasury;
    private final Clock clock;

    public PaymentOrderProcessingApplicationService(PaymentOrderJournal journal, TreasuryNotifier treasury, Clock clock) {
        this.journal = journal;
        this.treasury = treasury;
        this.clock = clock;
    }

    /**
     * The duplicate check, the exposure read and the record are three separate journal calls: like the
     * legacy, two concurrent submissions can both pass the limit. Fixing that is an infrastructure concern
     * (unique constraint on the reference, locking or a per-debtor sequence) — see SOLUTION.md.
     */
    @Override
    public String submit(String reference, String debtorIban, String creditorIban, String creditorBic, double amount,
                         String currency, LocalDate requestedExecutionDate, boolean urgent) {
        try {
            PaymentReference paymentReference = new PaymentReference(reference);
            if (journal.contains(paymentReference)) {
                return "DUPLICATE: " + reference;
            }
            ScheduledPayment scheduled = PaymentOrder.submit(paymentReference, debtorIban, creditorIban, creditorBic,
                amount, currency, requestedExecutionDate, urgent).scheduleAt(LocalDateTime.now(clock));
            double committed = journal.committedAmountInEur(scheduled.order().debtor(), scheduled.executionDate());
            double amountInEur = scheduled.order().money().inEur();
            ProcessedPaymentOrder processed = scheduled.withStatus(DailyLimit.statusFor(committed, amountInEur));
            journal.record(processed);
            if (processed.status() == ProcessingStatus.PENDING_APPROVAL) {
                treasury.approvalRequired(processed, committed + amountInEur);
            }
            return processed.render();
        } catch (PaymentOrderRejectedException e) {
            return switch (e.reason()) {
                case MISSING_REFERENCE -> "ERROR: missing reference";
                case INVALID_DEBTOR_IBAN -> "ERROR: invalid debtor IBAN";
                case INVALID_CREDITOR_IBAN -> "ERROR: invalid creditor IBAN";
                case IDENTICAL_ACCOUNTS -> "REJECTED: debtor and creditor accounts are identical";
                case INVALID_AMOUNT -> "ERROR: invalid amount";
                case UNSUPPORTED_CURRENCY -> "ERROR: unsupported currency";
                case BIC_REQUIRED -> "REJECTED: BIC required for SWIFT payment";
                case EXECUTION_DATE_IN_THE_PAST -> "REJECTED: execution date in the past";
            };
        }
    }
}
