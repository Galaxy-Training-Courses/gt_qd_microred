package pe.edu.galaxy.training.java.quarkus.loan.application.port.out;

/**
 * Lo que `LoanDomainService` necesita saber de un objeto de community-service:
 * quién es el propietario y si está disponible. Una sola llamada a `GET /api/objects/{id}`
 * ya trae ambos datos — este record evita pedirlo dos veces. Forma de dominio, no la respuesta
 * HTTP real de community.
 */
public record ObjectFact(Long objectId, Long ownerId, boolean unavailable) {}
