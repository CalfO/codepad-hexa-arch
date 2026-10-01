package fr.cbtw.interview.legacy;

import java.util.ArrayList;
import java.util.List;

import fr.cbtw.interview.application.port.in.CashSweepingUseCase;

public class CashSweepingService implements CashSweepingUseCase {
    static List<String> savedSweeps = new ArrayList<>();

    public String computeSweep(String sweepType, double subAccountBalance, double targetBalance,
                               double minimumTransfer, double masterAvailableBalance) {
        if (sweepType == null || sweepType.isBlank()) {
            return "ERROR: missing sweep type";
        }
        if (!sweepType.equals("ZERO_BALANCING") && !sweepType.equals("TARGET_BALANCING")
            && !sweepType.equals("THRESHOLD")) {
            return "ERROR: unknown sweep type";
        }
        if (minimumTransfer < 0) {
            return "ERROR: invalid minimum transfer";
        }
        double target;
        if (sweepType.equals("ZERO_BALANCING")) {
            target = 0;
        } else {
            if (targetBalance < 0) {
                return "ERROR: negative target balance";
            }
            target = targetBalance;
        }
        if (sweepType.equals("THRESHOLD") && subAccountBalance <= target) {
            return "NO_SWEEP: balance below threshold";
        }
        double delta = Math.round((subAccountBalance - target) * 100) / 100.0;
        if (Math.abs(delta) < minimumTransfer) {
            return "NO_SWEEP: below minimum transfer";
        }
        String instruction;
        if (delta > 0) {
            instruction = "TO_MASTER=" + delta;
        } else {
            double needed = -delta;
            if (masterAvailableBalance <= 0) {
                return "REJECTED: master account cannot fund";
            }
            if (masterAvailableBalance < needed) {
                instruction = "FROM_MASTER=" + masterAvailableBalance + ";PARTIAL";
            } else {
                instruction = "FROM_MASTER=" + needed;
            }
        }
        savedSweeps.add(sweepType + ":" + instruction);
        return "SWEEP: " + instruction;
    }
}
