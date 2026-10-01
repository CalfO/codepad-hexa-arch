package fr.cbtw.interview.domain.funds;

public record FundsReservation(FundsDecision decision, double paymentAmount, double remainingAvailable) {
    public String render() {
        return decision + ": remaining=" + remainingAvailable;
    }
}
