package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.jboss.logging.MDC;

/**
 * Copia el `requestId` del MDC (puesto por {@code RequestIdFilter}
 * para esta solicitud) a la cabecera saliente hacia community-service
 * — así sus logs también quedan correlacionados con los de loan-service.
 * Registrado en {@link CommunityRestClient} vía {@code @RegisterProvider}.
 */
public class RequestIdClientFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) {
        Object requestId = MDC.get("requestId");
        if (requestId != null) {
            requestContext.getHeaders().add("X-Request-Id", requestId.toString());
        }
    }
}
