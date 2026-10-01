package fr.cbtw.interview.application.port.in;

public interface CashSweepingUseCase {
    String computeSweep(String sweepType, double subAccountBalance, double targetBalance,
                        double minimumTransfer, double masterAvailableBalance);
}
