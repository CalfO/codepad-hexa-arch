package fr.cbtw.interview.application.port.out.paymentprocessing;

import fr.cbtw.interview.domain.paymentprocessing.ProcessedPaymentOrder;

/** Tells the treasury desk that an order exceeds the debtor's daily limit and needs approval. */
public interface TreasuryNotifier {
    void approvalRequired(ProcessedPaymentOrder order, double committedInEur);
}
