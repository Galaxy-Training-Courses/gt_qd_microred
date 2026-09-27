package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Forma exacta de GET /api/loans/{id}. */
public record LoanResponse(
        Long id,
        Long requesterId,
        LocalDate requestedFrom,
        LocalDate requestedUntil,
        String status,
        String reason,
        Instant createdAt,
        List<LoanItemResponse> items
) {}
