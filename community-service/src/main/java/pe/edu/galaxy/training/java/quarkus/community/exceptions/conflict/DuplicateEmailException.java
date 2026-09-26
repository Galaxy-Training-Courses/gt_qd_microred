package pe.edu.galaxy.training.java.quarkus.community.exceptions.conflict;

public class DuplicateEmailException extends ConflictException {
    public DuplicateEmailException(String email) {
        super("An user with email " + email + " already exists");
    }
}
