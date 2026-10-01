package fr.cbtw.interview.domain.sweeping;

public final class SweepRule {
    private final SweepType type;
    private final double targetBalance;
    private final double minimumTransfer;

    private SweepRule(SweepType type, double targetBalance, double minimumTransfer) {
        this.type = type;
        this.targetBalance = targetBalance;
        this.minimumTransfer = minimumTransfer;
    }

    // Validation order mirrors the legacy service. Zero balancing ignores the requested target
    // (even a negative one), as the legacy does.
    public static SweepRule of(String sweepTypeCode, double targetBalance, double minimumTransfer) {
        SweepType type = SweepType.fromCode(sweepTypeCode);
        if (minimumTransfer < 0) {
            throw new InvalidSweepRuleException(SweepRuleRejection.INVALID_MINIMUM_TRANSFER);
        }
        if (type == SweepType.ZERO_BALANCING) {
            return new SweepRule(type, 0, minimumTransfer);
        }
        if (targetBalance < 0) {
            throw new InvalidSweepRuleException(SweepRuleRejection.NEGATIVE_TARGET_BALANCE);
        }
        return new SweepRule(type, targetBalance, minimumTransfer);
    }

    /**
     * Legacy quirks kept for parity (see SOLUTION.md): a balanced sub-account with a minimum transfer of 0
     * yields "FROM_MASTER=-0.0" (negating a 0.0 delta), and a partial funding reports the master balance
     * unrounded while full transfers are rounded to the cent.
     */
    public SweepDecision applyTo(double subAccountBalance, double masterAvailableBalance) {
        if (type == SweepType.THRESHOLD && subAccountBalance <= targetBalance) {
            return new SweepDecision.NoSweep("balance below threshold");
        }
        double delta = Math.round((subAccountBalance - targetBalance) * 100) / 100.0;
        if (Math.abs(delta) < minimumTransfer) {
            return new SweepDecision.NoSweep("below minimum transfer");
        }
        if (delta > 0) {
            return new SweepDecision.SweepToMaster(delta);
        }
        double needed = -delta;
        if (masterAvailableBalance <= 0) {
            return new SweepDecision.FundingRefused();
        }
        if (masterAvailableBalance < needed) {
            return new SweepDecision.FundingFromMaster(masterAvailableBalance, true);
        }
        return new SweepDecision.FundingFromMaster(needed, false);
    }
}
