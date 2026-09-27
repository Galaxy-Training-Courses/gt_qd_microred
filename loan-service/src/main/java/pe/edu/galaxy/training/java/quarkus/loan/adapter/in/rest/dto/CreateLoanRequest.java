package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * POST /api/loans. `items` no vacío se resuelve con @NotEmpty
 * (400 vía ConstraintViolationExceptionMapper); el resto de reglas
 * (fechas, existencia en community, propietario, disponibilidad) son
 * semánticas y se validan en LoanService, no aquí.
 */
public record CreateLoanRequest(
        @NotNull Long requesterId,
        @NotNull LocalDate requestedFrom,
        @NotNull LocalDate requestedUntil,
        @NotBlank String reason,
        @NotEmpty List<@Valid CreateLoanItemRequest> items
) {}
