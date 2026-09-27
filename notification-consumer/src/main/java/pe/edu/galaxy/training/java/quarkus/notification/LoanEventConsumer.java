package pe.edu.galaxy.training.java.quarkus.notification;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

/**
 * Único consumidor del módulo. Se limita a registrar el
 * evento recibido como log — `quarkus-logging-json` serializa cada línea
 * (incluido este mensaje ya formateado) como un objeto JSON, que es lo que
 * exige el DoD. No hay base de datos, no hay reenvío de correos, no hay
 * lógica de negocio: notification-consumer no la necesita.
 * {@code event.requestId()} vuelve a entrar al MDC antes
 * de loguear — así esta línea queda correlacionada con las de loan-service
 * para la misma solicitud, cerrando la traza de punta a punta. Se limpia en
 * el `finally`: el hilo del consumidor de Kafka se reutiliza entre mensajes.
 */
@ApplicationScoped
public class LoanEventConsumer {

    private static final Logger LOG = Logger.getLogger(LoanEventConsumer.class);

    @Incoming("loan-events")
    public void onLoanEvent(LoanEvent event) {
        if (event.requestId() != null) {
            MDC.put("requestId", event.requestId());
        }
        try {
            LOG.infof("Evento de prestamo recibido: eventType=%s eventId=%s loanId=%s requesterId=%s "
                            + "objectIds=%s status=%s",
                    event.eventType(), event.eventId(), event.loanId(), event.requesterId(),
                    event.objectIds(), event.status());
        } finally {
            MDC.remove("requestId");
        }
    }
}
