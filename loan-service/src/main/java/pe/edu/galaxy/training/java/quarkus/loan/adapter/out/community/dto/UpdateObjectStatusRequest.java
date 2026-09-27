package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community.dto;

/**
 * Cuerpo de `PATCH /api/objects/{id}`: {@code loan-service}
 * es quien mueve el objeto entre `RESERVED`/`AVAILABLE` al aprobar/devolver
 * un préstamo.
 */
public record UpdateObjectStatusRequest(String status) {}
