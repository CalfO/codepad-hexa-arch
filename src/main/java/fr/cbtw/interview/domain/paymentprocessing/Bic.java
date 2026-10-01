package fr.cbtw.interview.domain.paymentprocessing;

public final class Bic {
    private Bic() {
    }

    public static boolean isUsable(String value) {
        return value != null && (value.length() == 8 || value.length() == 11);
    }
}
