package pe.edu.galaxy.training.java.quarkus.community.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.util.UUID;

/**
 * Lee el `X-Request-Id` que propaga `loan-service` (o genera uno nuevo si la solicitud llega directo),
 * lo guarda en el MDC y lo devuelve en la respuesta — así los logs de
 * community-service quedan correlacionados con los de quien la llamó.
 * Mismo comportamiento que el filtro homónimo de loan-service; se duplica
 * porque cada módulo es independiente.
 */
@Provider
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(RequestIdFilter.class);
    public static final String HEADER = "X-Request-Id";
    private static final String MDC_KEY = "requestId";
    private static final String START_KEY = "requestStartNanos";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String requestId = requestContext.getHeaderString(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, requestId);
        requestContext.setProperty(MDC_KEY, requestId);
        requestContext.setProperty(START_KEY, System.nanoTime());
        LOG.infof("Solicitud recibida: %s %s", requestContext.getMethod(), requestContext.getUriInfo().getPath());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object requestId = requestContext.getProperty(MDC_KEY);
        if (requestId != null) {
            responseContext.getHeaders().add(HEADER, requestId);
            // P6: se repone por si la respuesta se procesa en otro hilo que el de la solicitud
            MDC.put(MDC_KEY, requestId);
        }
        LOG.infof("Solicitud completada: %s %s -> %d (%d ms)", requestContext.getMethod(),
                requestContext.getUriInfo().getPath(), responseContext.getStatus(), elapsedMs(requestContext));
        MDC.remove(MDC_KEY);
    }

    private long elapsedMs(ContainerRequestContext requestContext) {
        Object start = requestContext.getProperty(START_KEY);
        return start instanceof Long startNanos ? (System.nanoTime() - startNanos) / 1_000_000 : -1;
    }
}
