package pe.edu.galaxy.training.java.quarkus.loan.domain.model;

/** Detalle de la solicitud: objeto de dominio puro. */
public record LoanRequestItem(
        Long id,
        Long objectId,
        String notes
) {}
