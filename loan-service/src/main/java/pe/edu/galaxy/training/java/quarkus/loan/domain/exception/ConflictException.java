package pe.edu.galaxy.training.java.quarkus.loan.domain.exception;

/**
 * 409: el objeto solicitado está UNAVAILABLE, o la transición de estado
 * pedida no es válida desde el estado actual. Una sola clase con factories
 * estáticas, mismo criterio que {@link NotFoundException}.
 */
public class ConflictException extends RuntimeException {
    private ConflictException(String message) {
        super(message);
    }

    public static ConflictException objectUnavailable(Long objectId) {
        return new ConflictException("Object with id " + objectId + " is UNAVAILABLE and can not be requested.");
    }

    public static ConflictException invalidTransition(Object from, Object to) {
        return new ConflictException("Can not transition a loan request from '" + from + "' to '" + to + "'.");
    }
}
