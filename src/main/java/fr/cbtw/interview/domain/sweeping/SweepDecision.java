package fr.cbtw.interview.domain.sweeping;

/** Outcome of applying a sweep rule. Only transfers are recorded; the other outcomes leave no trace. */
public sealed interface SweepDecision {
    String render();

    default boolean isTransfer() {
        return false;
    }

    record NoSweep(String reason) implements SweepDecision {
        public String render() {
            return "NO_SWEEP: " + reason;
        }
    }

    record FundingRefused() implements SweepDecision {
        public String render() {
            return "REJECTED: master account cannot fund";
        }
    }

    record SweepToMaster(double amount) implements SweepDecision {
        public String render() {
            return "SWEEP: TO_MASTER=" + amount;
        }

        public boolean isTransfer() {
            return true;
        }
    }

    record FundingFromMaster(double amount, boolean partial) implements SweepDecision {
        public String render() {
            return "SWEEP: FROM_MASTER=" + amount + (partial ? ";PARTIAL" : "");
        }

        public boolean isTransfer() {
            return true;
        }
    }
}
