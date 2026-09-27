package pe.edu.galaxy.training.java.quarkus.loan.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Solicitud de préstamo: objeto de dominio puro, sinanotaciones JPA
 * ni Jackson. {@code id} es {@code null} para una solicitud todavía
 * no persistida — la asigna {@code LoanRepositoryPort.save(...)}.
 */
public record LoanRequest(
        Long id,
        Long requesterId,
        LocalDate requestedFrom,
        LocalDate requestedUntil,
        LoanStatus status,
        String reason,
        Instant createdAt,
        List<LoanRequestItem> items
) {
    public LoanRequest withStatus(LoanStatus newStatus) {
        return new LoanRequest(id, requesterId, requestedFrom, requestedUntil, newStatus, reason, createdAt, items);
    }
}
