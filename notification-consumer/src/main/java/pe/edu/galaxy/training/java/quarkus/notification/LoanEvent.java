package pe.edu.galaxy.training.java.quarkus.notification;

import java.time.Instant;
import java.util.List;

/**
 * Espejo local del evento que publica loan-service en el tópico
 * `loan-events`. Copia deliberada, no una dependencia
 * compartida: son módulos Maven independientes — mismo criterio que los DTO espejo de
 * community-service en loan-service.
 * `requestId`: lo agrega loan-service desde su filtro de correlación;
 * este módulo lo vuelve a poner en su propio MDC antes de loguear
 * ({@link LoanEventConsumer}), cerrando la traza de punta a punta.
 */
public record LoanEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        Long loanId,
        Long requesterId,
        List<Long> objectIds,
        String status,
        String requestId
) {}
