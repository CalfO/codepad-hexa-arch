package fr.cbtw.interview.domain.paymentprocessing;

import java.util.regex.Pattern;

/** Structural check only (country, check digits, BBAN length), no mod-97 validation — like the legacy. */
public record Iban(String value) {
    private static final Pattern FORMAT = Pattern.compile("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}");

    public Iban {
        if (!isWellFormed(value)) {
            throw new IllegalArgumentException("Malformed IBAN");
        }
    }

    public static boolean isWellFormed(String value) {
        return value != null && FORMAT.matcher(value).matches();
    }

    public String countryCode() {
        return value.substring(0, 2);
    }
}
