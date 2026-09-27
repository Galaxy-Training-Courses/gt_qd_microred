package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community.dto.CommunityObjectResponse;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community.dto.CommunityUserResponse;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community.dto.UpdateObjectStatusRequest;

/**
 * REST Client Reactive hacia `community-service`. {@code configKey = "community-api"}
 * referencia {@code quarkus.rest-client.community-api.url} en application.properties.
 * Los métodos devuelven {@code Uni<T>} de forma nativa: no hace falta ningún adaptador
 * extra para Mutiny.
 * {@link RequestIdClientFilter} propaga el `requestId` de la solicitud
 * actual como cabecera saliente, para que los logs de community-service
 * también queden correlacionados.
 */
@RegisterRestClient(configKey = "community-api")
@RegisterProvider(RequestIdClientFilter.class)
@Path("/api")
public interface CommunityRestClient {

    @GET
    @Path("/users/{id}")
    Uni<CommunityUserResponse> getUser(@PathParam("id") Long id);

    @GET
    @Path("/objects/{id}")
    Uni<CommunityObjectResponse> getObject(@PathParam("id") Long id);

    @PATCH
    @Path("/objects/{id}")
    Uni<CommunityObjectResponse> patchObjectStatus(@PathParam("id") Long id, UpdateObjectStatusRequest body);
}
