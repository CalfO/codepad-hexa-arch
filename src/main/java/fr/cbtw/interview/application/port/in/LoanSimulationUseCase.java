package fr.cbtw.interview.application.port.in;

public interface LoanSimulationUseCase {
    String simulate(double amount, double rate, int months, int applicantAge, double monthlyIncome);
}
