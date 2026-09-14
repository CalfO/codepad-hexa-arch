package domain.model;

public record ApplicantIdentity(String fullName, String address) {
    public ApplicantIdentity {
        if (isBlank(fullName) || isBlank(address)) {
            throw new KycRejectedException(KycRejection.INCOMPLETE_PROFILE);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
