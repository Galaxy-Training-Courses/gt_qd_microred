package pe.edu.galaxy.training.java.quarkus.community.entities.object;

import pe.edu.galaxy.training.java.quarkus.community.common.PersistenceConstants;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.InvalidFieldValueException;

/**
 * Estado físico del objeto.
 */
public enum ObjectCondition {
    NEW,
    GOOD,
    USED,
    WORN;

    public static ObjectCondition parseCondition(String condition) {
        try {
            return ObjectCondition.valueOf(condition);
        } catch (IllegalArgumentException _) {
            throw InvalidFieldValueException.invalidEnum(PersistenceConstants.CONDITION_FIELD, condition);
        }
    }
}
