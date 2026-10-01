package fr.cbtw.interview.domain.payment;

import java.time.LocalDate;

public record ScheduledPaymentOrder(PaymentOrder order, LocalDate executionDate, ExecutionChannel channel) {
    public String render() {
        return "ACCEPTED: execution=" + executionDate + ";channel=" + channel;
    }
}
