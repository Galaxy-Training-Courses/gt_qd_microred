package pe.edu.galaxy.training.java.quarkus.loan.domain.exception;

/**
 * 404 para las tres referencias que puede tener una solicitud. Una sola
 * clase con factories estáticas en vez de una subclase por caso — igual
 * técnica que {@link InvalidFieldValueException}, un solo mapper resuelve
 * los tres.
 */
public class NotFoundException extends RuntimeException {
    private static final String LOAN_REQUEST_NOT_FOUND = "Loan request with id %d does not exist.";
    private static final String REQUESTER_NOT_FOUND = "Requester (user) with id %d does not exist.";
    private static final String OBJECT_NOT_FOUND = "Object with id %d does not exist.";

    private NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException loanNotFound(Long id) {
        return new NotFoundException(LOAN_REQUEST_NOT_FOUND.formatted(id));
    }

    public static NotFoundException requesterNotFound(Long id) {
        return new NotFoundException(REQUESTER_NOT_FOUND.formatted(id));
    }

    public static NotFoundException objectNotFound(Long id) {
        return new NotFoundException(OBJECT_NOT_FOUND.formatted(id));
    }
}
