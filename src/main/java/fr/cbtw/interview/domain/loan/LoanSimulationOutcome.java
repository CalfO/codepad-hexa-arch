package fr.cbtw.interview.domain.loan;

public record LoanSimulationOutcome(double monthlyPayment) {
    public String render() {
        return "APPROVED: monthly=" + monthlyPayment;
    }
}
