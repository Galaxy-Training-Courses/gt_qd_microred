package pe.edu.galaxy.training.java.quarkus.community.exceptions.conflict;

public class ObjectReservedException extends ConflictException {
    public ObjectReservedException(Long id) {
        super("Object with id '" + id + "' is reserved; it can not be deleted");
    }
}
