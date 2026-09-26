package pe.edu.galaxy.training.java.quarkus.community.exceptions;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(Long id) {
        super("Object with id " + id + " does not exist.");
    }
}
