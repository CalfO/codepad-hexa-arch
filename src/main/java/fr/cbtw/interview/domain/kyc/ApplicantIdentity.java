package fr.cbtw.interview.domain.kyc;

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
