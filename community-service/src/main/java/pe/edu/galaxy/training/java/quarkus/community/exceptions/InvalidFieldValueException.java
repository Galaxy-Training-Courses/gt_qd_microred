package pe.edu.galaxy.training.java.quarkus.community.exceptions;

/**
 * 400 genérico para un valor de campo sintácticamente válido pero
 * semánticamente inválido:
 * <ul>
 *  <li>un enum inexistente</li>
 *  <li>un `sort` fuera de la lista blanca</li>
 *  <li>un `size` fuera de rango.</li>
 * </ul>
 */
public class InvalidFieldValueException extends RuntimeException {
    public InvalidFieldValueException(String message) {
        super(message);
    }

    public static InvalidFieldValueException invalidEnum(String field, String value) {
        return new InvalidFieldValueException("Invalid value for '" + field + "': " + value);
    }
}
