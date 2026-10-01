package fr.cbtw.interview.domain.payment;

import java.time.LocalTime;
import java.util.Arrays;

public enum PaymentCurrency {
    EUR(LocalTime.of(16, 0)),
    USD(LocalTime.of(14, 0)),
    GBP(LocalTime.of(14, 0)),
    CHF(LocalTime.of(14, 0));

    private final LocalTime cutOff;

    PaymentCurrency(LocalTime cutOff) {
        this.cutOff = cutOff;
    }

    public static PaymentCurrency fromCode(String code) {
        return Arrays.stream(values()).filter(c -> c.name().equals(code)).findFirst()
            .orElseThrow(() -> new PaymentOrderRejectedException(PaymentOrderRejection.UNSUPPORTED_CURRENCY));
    }

    public LocalTime cutOff() {
        return cutOff;
    }
}
