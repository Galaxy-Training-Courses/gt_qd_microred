package pe.edu.galaxy.training.java.quarkus.loan.domain.exception;

/**
 * 400 genérico para un valor sintácticamente válido pero semánticamente
 * inválido.
 */
public class InvalidFieldValueException extends RuntimeException {
    public InvalidFieldValueException(String message) {
        super(message);
    }

    public static InvalidFieldValueException invalidEnum(String field, String value) {
        return new InvalidFieldValueException("Invalid value for '" + field + "': " + value);
    }

    public static InvalidFieldValueException invalidDateRange(String requestedFrom, String requestedUntil) {
        return new InvalidFieldValueException(
                "'requestedFrom' (" + requestedFrom + ") must be before 'requestedUntil' (" + requestedUntil + ")"
        );
    }
}
