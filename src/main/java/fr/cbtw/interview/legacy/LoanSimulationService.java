package fr.cbtw.interview.legacy;

import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.in.LoanSimulationUseCase;

public class LoanSimulationService implements LoanSimulationUseCase {
    static List<String> savedSimulations = new ArrayList<>();

    public String simulate(double amount, double rate, int months, int applicantAge, double monthlyIncome) {
        if (amount <= 0 || rate < 0 || months <= 0) {
            return "ERROR: invalid input";
        }
        if (applicantAge < 18 || applicantAge > 75) {
            return "ERROR: applicant not eligible";
        }
        double monthlyRate = rate / 12 / 100;
        double monthlyPayment = (amount * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -months));
        double debtRatio = monthlyPayment / monthlyIncome;
        if (debtRatio > 0.35) {
            return "REJECTED: debt ratio too high";
        }
        String result = "APPROVED: monthly=" + monthlyPayment;
        savedSimulations.add(result);
        return result;
    }
}
