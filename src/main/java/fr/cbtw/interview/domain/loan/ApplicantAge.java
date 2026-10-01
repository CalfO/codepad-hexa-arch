package fr.cbtw.interview.domain.loan;

public record ApplicantAge(int years) {
    public boolean isEligible() {
        return years >= 18 && years <= 75;
    }
}
