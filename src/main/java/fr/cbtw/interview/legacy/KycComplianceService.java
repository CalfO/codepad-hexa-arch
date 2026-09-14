package fr.cbtw.interview.legacy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KycComplianceService {
    static List<String> savedChecks = new ArrayList<>();

    public String checkCompliance(String documentType, LocalDate issueDate, LocalDate expiryDate,
                                   String fullName, String address, double declaredIncome,
                                   boolean isPoliticallyExposed) {
        if (documentType == null || documentType.isBlank()) {
            return "ERROR: missing document type";
        }
        if (!documentType.equals("PASSPORT") && !documentType.equals("ID_CARD") && !documentType.equals("RESIDENCE_PERMIT")) {
            return "ERROR: unsupported document type";
        }
        if (fullName == null || fullName.isBlank() || address == null || address.isBlank()) {
            return "REJECTED: incomplete profile";
        }
        if (expiryDate == null || expiryDate.isBefore(LocalDate.now())) {
            return "REJECTED: expired document";
        }
        if (issueDate == null || issueDate.isAfter(LocalDate.now())) {
            return "ERROR: invalid issue date";
        }
        int riskScore = 0;
        if (isPoliticallyExposed) {
            riskScore += 50;
        }
        if (declaredIncome <= 0) {
            riskScore += 20;
        }
        if (declaredIncome > 0 && declaredIncome < 1000) {
            riskScore += 10;
        }
        String decision;
        if (riskScore >= 50) {
            decision = "MANUAL_REVIEW";
        } else if (riskScore >= 20) {
            decision = "APPROVED_WITH_MONITORING";
        } else {
            decision = "APPROVED";
        }
        String result = "STATUS:" + decision + ";RISK:" + riskScore;
        savedChecks.add(result);
        return result;
    }
}