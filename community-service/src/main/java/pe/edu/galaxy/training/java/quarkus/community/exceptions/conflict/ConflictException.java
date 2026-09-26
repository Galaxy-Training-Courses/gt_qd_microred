package pe.edu.galaxy.training.java.quarkus.community.exceptions.conflict;

/**
 * Base de las excepciones que representan un conflicto de estado (409):
 * UserHasReservedObjectsException, ObjectReservedException. Un único
 * ConflictExceptionMapper las resuelve a todas.
 */
public abstract class ConflictException extends RuntimeException {
    protected ConflictException(String message) {
        super(message);
    }
}
