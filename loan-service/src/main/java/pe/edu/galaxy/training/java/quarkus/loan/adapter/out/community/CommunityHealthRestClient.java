package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Interfaz separada de {@link CommunityRestClient} solo porque esta necesita
 * pedir `/q/health/live` (raíz), no `/api/...` — mismo `configKey`
 * (`community-api`, misma URL base) pero sin el `@Path("/api")` de clase
 * que llevaría a `/api/q/health/live`. La usa
 * {@link CommunityServiceHealthCheck}.
 */
@RegisterRestClient(configKey = "community-api")
public interface CommunityHealthRestClient {

    @GET
    @Path("/q/health/live")
    Uni<Response> checkLiveness();
}
