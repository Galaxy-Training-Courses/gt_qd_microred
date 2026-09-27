package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.messaging;

import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.LoanEventPublisherPort;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequestItem;

import java.time.Clock;
import java.util.UUID;

/**
 * Implementa {@link LoanEventPublisherPort} publicando en el tópico `loan-events`
 * `publish` nunca propaga una falla de Kafka: se llama después de que la
 * transacción de negocio ya se confirmó (`LoanApplicationService` invoca
 * `save(...)` antes que `publish(...)`, dentro del mismo método
 * `@Transactional`) — si esto lanzara, JTA revertiría una operación que el
 * cliente ya ve como exitosa. Envío fire-and-forget con `Emitter`, la falla
 * se registra en el log, nunca se relanza
 * `clock` (en vez de `Instant.now()` directo) hace testeable
 * `occurredAt`; `requestId` sale del MDC que ya dejó `RequestIdFilter` para
 * esta misma solicitud — así el evento que recibe notification-consumer
 * queda correlacionado con los logs de loan-service.
 */
@ApplicationScoped
public class LoanEventProducer implements LoanEventPublisherPort {

    private static final Logger LOG = Logger.getLogger(LoanEventProducer.class);

    @Channel("loan-events")
    Emitter<Record<Long, LoanEvent>> emitter;

    @Inject
    Clock clock;

    @Override
    public void publish(String eventType, LoanRequest loanRequest) {
        LoanEvent event = new LoanEvent(
                UUID.randomUUID().toString(),
                eventType,
                clock.instant(),
                loanRequest.id(),
                loanRequest.requesterId(),
                loanRequest.items().stream().map(LoanRequestItem::objectId).toList(),
                loanRequest.status().name(),
                MDC.get("requestId") != null ? MDC.get("requestId").toString() : null
        );

        emitter.send(Record.of(loanRequest.id(), event))
                .exceptionally(failure -> {
                    LOG.error(
                            "No se pudo publicar " + eventType + " para la solicitud "
                                    + loanRequest.id(), failure);
                    return null;
                });
    }
}
