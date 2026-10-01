package fr.cbtw.interview.domain.funds;

import java.util.Arrays;

public enum AccountStatus {
    ACTIVE, BLOCKED, CLOSED;

    public static AccountStatus fromCode(String code) {
        return Arrays.stream(values()).filter(s -> s.name().equals(code)).findFirst()
            .orElseThrow(() -> new FundsCheckRejectedException(FundsCheckRejection.UNKNOWN_ACCOUNT_STATUS));
    }

    public void ensureCanBeDebited() {
        switch (this) {
            case CLOSED -> throw new FundsCheckRejectedException(FundsCheckRejection.ACCOUNT_CLOSED);
            case BLOCKED -> throw new FundsCheckRejectedException(FundsCheckRejection.ACCOUNT_BLOCKED);
            case ACTIVE -> { }
        }
    }
}
