package domain.model;

public record ApplicantAge(int years) {
    public boolean isEligible() {
        return years >= 18 && years <= 75;
    }
}
