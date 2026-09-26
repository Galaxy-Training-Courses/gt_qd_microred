package pe.edu.galaxy.training.java.quarkus.community.exceptions.conflict;

/**
 * DELETE /api/users/{id}: 409 si el usuario tiene objetos
 * con préstamos activos. community-service no conoce loan-service; usa como
 * proxy que el propio objeto esté en estado RESERVED (solo loan-service lo
 * pone así, vía PATCH /api/objects/{id}).
 */
public class UserHasReservedObjectsException extends ConflictException {
    public UserHasReservedObjectsException(Long userId) {
        super("User with id '" + userId + "' has reserved objects; it can not be deleted");
    }
}
