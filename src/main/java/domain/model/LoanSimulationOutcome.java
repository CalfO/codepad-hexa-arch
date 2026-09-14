package domain.model;

public record LoanSimulationOutcome(double monthlyPayment) {
    public String render() {
        return "APPROVED: monthly=" + monthlyPayment;
    }
}
