package pe.edu.galaxy.training.java.quarkus.community.entities.user;

import pe.edu.galaxy.training.java.quarkus.community.exceptions.InvalidFieldValueException;

/**
 * Estado del miembro de la comunidad.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE;

    public static UserStatus parseStatus(String status) {
        try {
            return UserStatus.valueOf(status);
        } catch (IllegalArgumentException _) {
            throw InvalidFieldValueException.invalidEnum("status", status);
        }
    }
}
