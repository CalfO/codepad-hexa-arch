package fr.cbtw.interview.infrastructure.paymentprocessing;

import fr.cbtw.interview.application.port.out.paymentprocessing.TreasuryNotifier;
import fr.cbtw.interview.domain.paymentprocessing.ProcessedPaymentOrder;

/** Same console message as the legacy; a real adapter would publish to the treasury desk's workflow. */
public final class ConsoleTreasuryNotifier implements TreasuryNotifier {
    @Override
    public void approvalRequired(ProcessedPaymentOrder order, double committedInEur) {
        System.out.println("[TREASURY] approval required for " + order.reference().value() + " ("
            + order.debtor().value() + ", " + committedInEur + " EUR committed on " + order.executionDate() + ")");
    }
}
