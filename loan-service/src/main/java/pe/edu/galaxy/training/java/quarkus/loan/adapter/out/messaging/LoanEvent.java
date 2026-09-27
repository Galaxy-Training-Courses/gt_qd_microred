package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.messaging;

import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.LoanEventPublisherPort;

import java.time.Instant;
import java.util.List;

/**
 * Envelope publicado en el tópico `loan-events`. Detalle de
 * serialización del adaptador de salida — ni el dominio ni la aplicación lo
 * conocen; {@link LoanEventPublisherPort} solo recibe `eventType` y el
 * {@code LoanRequest} de dominio.
 * `requestId`: lo agrega {@code LoanEventProducer} desde
 * el MDC del filtro de correlación (`RequestIdFilter`) — permite seguir una
 * solicitud desde el `curl` hasta la línea de log de notification-consumer.
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
