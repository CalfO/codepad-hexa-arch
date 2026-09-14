package domain.model;

public record DeclaredIncome(double value) {
    public boolean isNonPositive() {
        return value <= 0;
    }

    public boolean isLowPositiveIncome(double threshold) {
        return value > 0 && value < threshold;
    }
}
