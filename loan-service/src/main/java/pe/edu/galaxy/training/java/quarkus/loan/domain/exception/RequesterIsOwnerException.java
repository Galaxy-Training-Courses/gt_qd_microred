package pe.edu.galaxy.training.java.quarkus.loan.domain.exception;

/**
 * 422: el solicitante no puede ser el propietario del objeto.
 * Primer uso de 422 en el proyecto (tabla de códigos de estado) —
 * distinto de 400 (payload sintácticamente inválido) y de 409 (conflicto de
 * estado): aquí el payload es válido, pero viola una regla de negocio.
 */
public class RequesterIsOwnerException extends RuntimeException {
    public RequesterIsOwnerException(Long requesterId, Long objectId) {
        super("Requester " + requesterId + " is the owner of object " + objectId + "; can not request own object.");
    }
}
