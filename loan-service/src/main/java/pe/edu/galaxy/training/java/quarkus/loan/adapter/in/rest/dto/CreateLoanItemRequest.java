package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

/** Item de `POST /api/loans`. */
public record CreateLoanItemRequest(
        @NotNull Long objectId,
        String notes
) {}
