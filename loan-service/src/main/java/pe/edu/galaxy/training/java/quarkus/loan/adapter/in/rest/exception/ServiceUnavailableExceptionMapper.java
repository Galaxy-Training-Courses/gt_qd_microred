package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.exception;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.ServiceUnavailableException;

@Provider
public class ServiceUnavailableExceptionMapper implements ExceptionMapper<ServiceUnavailableException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(ServiceUnavailableException exception) {
        ErrorResponse body = ErrorResponse.of(
                503,
                "SERVICE_UNAVAILABLE",
                exception.getMessage(),
                requestContext.getUriInfo().getPath()
        );
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(body).build();
    }
}
