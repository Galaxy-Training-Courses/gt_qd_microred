package pe.edu.galaxy.training.java.quarkus.community.entities.object;

import pe.edu.galaxy.training.java.quarkus.community.common.PersistenceConstants;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.InvalidFieldValueException;

/**
 * Estados sugeridos para la disponibilidad de un objeto de préstamo. l
 * oan-service consulta y modifica este estado
 */
public enum ObjectStatus {
    AVAILABLE,
    RESERVED,
    UNAVAILABLE;

    public static ObjectStatus parseStatus(String status) {
        try {
            return ObjectStatus.valueOf(status);
        } catch (IllegalArgumentException _) {
            throw InvalidFieldValueException.invalidEnum(PersistenceConstants.STATUS_FIELD, status);
        }
    }
}
