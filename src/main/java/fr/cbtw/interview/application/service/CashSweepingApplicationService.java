package fr.cbtw.interview.application.service;

import fr.cbtw.interview.application.port.in.CashSweepingUseCase;
import fr.cbtw.interview.application.port.out.SweepInstructionRepository;
import fr.cbtw.interview.domain.sweeping.InvalidSweepRuleException;
import fr.cbtw.interview.domain.sweeping.SweepDecision;
import fr.cbtw.interview.domain.sweeping.SweepRule;

public final class CashSweepingApplicationService implements CashSweepingUseCase {
    private final SweepInstructionRepository<SweepDecision> repository;

    public CashSweepingApplicationService(SweepInstructionRepository<SweepDecision> repository) {
        this.repository = repository;
    }

    @Override
    public String computeSweep(String sweepType, double subAccountBalance, double targetBalance,
                               double minimumTransfer, double masterAvailableBalance) {
        try {
            SweepDecision decision = SweepRule.of(sweepType, targetBalance, minimumTransfer)
                .applyTo(subAccountBalance, masterAvailableBalance);
            if (decision.isTransfer()) {
                repository.save(decision);
            }
            return decision.render();
        } catch (InvalidSweepRuleException e) {
            return switch (e.reason()) {
                case MISSING_SWEEP_TYPE -> "ERROR: missing sweep type";
                case UNKNOWN_SWEEP_TYPE -> "ERROR: unknown sweep type";
                case INVALID_MINIMUM_TRANSFER -> "ERROR: invalid minimum transfer";
                case NEGATIVE_TARGET_BALANCE -> "ERROR: negative target balance";
            };
        }
    }
}
